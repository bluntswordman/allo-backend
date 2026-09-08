package com.allobank.splitbill.settlement;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SettlementCalculator {

	public SettlementCalculation calculate(List<ParticipantSnapshot> participants, List<ExpenseSnapshot> expenses) {
		Map<UUID, MutableBalance> balances = new HashMap<>();
		participants.forEach(participant -> balances.put(
				participant.id(), new MutableBalance(participant.id(), participant.name(), BigDecimal.ZERO)));

		BigDecimal totalExpenses = BigDecimal.ZERO;
		for (ExpenseSnapshot expense : expenses) {
			MutableBalance payer = requireParticipant(balances, expense.payerId());
			payer.add(expense.amount());
			totalExpenses = totalExpenses.add(expense.amount());

			BigDecimal allocated = BigDecimal.ZERO;
			for (ShareSnapshot share : expense.shares()) {
				requireParticipant(balances, share.participantId()).subtract(share.amount());
				allocated = allocated.add(share.amount());
			}
			if (allocated.compareTo(expense.amount()) != 0) {
				throw new IllegalStateException("Expense shares do not equal the expense amount");
			}
		}

		BigDecimal balanceSum = balances.values().stream()
				.map(MutableBalance::amount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (balanceSum.compareTo(BigDecimal.ZERO) != 0) {
			throw new IllegalStateException("Participant balances do not sum to zero");
		}

		List<MutableBalance> creditors = balances.values().stream()
				.filter(balance -> balance.amount().signum() > 0)
				.map(MutableBalance::copy)
				.sorted(balanceComparator())
				.toList();
		List<MutableBalance> debtors = balances.values().stream()
				.filter(balance -> balance.amount().signum() < 0)
				.map(balance -> new MutableBalance(balance.id(), balance.name(), balance.amount().abs()))
				.sorted(balanceComparator())
				.toList();

		List<Transfer> transfers = buildTransfers(debtors, creditors);
		List<ParticipantBalance> participantBalances = balances.values().stream()
				.map(balance -> new ParticipantBalance(balance.id(), balance.name(), balance.amount()))
				.sorted(Comparator.comparing(ParticipantBalance::participantName, String.CASE_INSENSITIVE_ORDER)
						.thenComparing(balance -> balance.participantId().toString()))
				.toList();
		return new SettlementCalculation(totalExpenses, participantBalances, transfers);
	}

	private List<Transfer> buildTransfers(List<MutableBalance> debtors, List<MutableBalance> creditors) {
		List<Transfer> transfers = new ArrayList<>();
		int debtorIndex = 0;
		int creditorIndex = 0;
		while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
			MutableBalance debtor = debtors.get(debtorIndex);
			MutableBalance creditor = creditors.get(creditorIndex);
			BigDecimal amount = debtor.amount().min(creditor.amount());

			transfers.add(new Transfer(
					debtor.id(), debtor.name(), creditor.id(), creditor.name(), amount));
			debtor.subtract(amount);
			creditor.subtract(amount);

			if (debtor.amount().signum() == 0) {
				debtorIndex++;
			}
			if (creditor.amount().signum() == 0) {
				creditorIndex++;
			}
		}
		return List.copyOf(transfers);
	}

	private Comparator<MutableBalance> balanceComparator() {
		return Comparator.comparing(MutableBalance::amount).reversed()
				.thenComparing(balance -> balance.id().toString());
	}

	private MutableBalance requireParticipant(Map<UUID, MutableBalance> balances, UUID participantId) {
		MutableBalance balance = balances.get(participantId);
		if (balance == null) {
			throw new IllegalStateException("Expense references a participant outside the group");
		}
		return balance;
	}

	public record ParticipantSnapshot(UUID id, String name) {
	}

	public record ShareSnapshot(UUID participantId, BigDecimal amount) {
	}

	public record ExpenseSnapshot(UUID payerId, BigDecimal amount, List<ShareSnapshot> shares) {
	}

	public record ParticipantBalance(UUID participantId, String participantName, BigDecimal netBalance) {
	}

	public record Transfer(
			UUID fromParticipantId,
			String fromParticipantName,
			UUID toParticipantId,
			String toParticipantName,
			BigDecimal amount) {
	}

	public record SettlementCalculation(
			BigDecimal totalExpenses,
			List<ParticipantBalance> balances,
			List<Transfer> settlements) {
	}

	private static final class MutableBalance {
		private final UUID id;
		private final String name;
		private BigDecimal amount;

		private MutableBalance(UUID id, String name, BigDecimal amount) {
			this.id = id;
			this.name = name;
			this.amount = amount;
		}

		private UUID id() {
			return id;
		}

		private String name() {
			return name;
		}

		private BigDecimal amount() {
			return amount;
		}

		private void add(BigDecimal value) {
			amount = amount.add(value);
		}

		private void subtract(BigDecimal value) {
			amount = amount.subtract(value);
		}

		private MutableBalance copy() {
			return new MutableBalance(id, name, amount);
		}
	}
}
