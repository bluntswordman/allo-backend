package com.allobank.splitbill.settlement;

import com.allobank.splitbill.common.ResourceNotFoundException;
import com.allobank.splitbill.expense.Expense;
import com.allobank.splitbill.expense.ExpenseRepository;
import com.allobank.splitbill.expense.ExpenseShare;
import com.allobank.splitbill.group.BillGroup;
import com.allobank.splitbill.group.BillGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SettlementService {

	private final BillGroupRepository groupRepository;
	private final ExpenseRepository expenseRepository;
	private final SettlementCalculator settlementCalculator;
	private final ServiceChargeCalculator serviceChargeCalculator;

	public SettlementService(
			BillGroupRepository groupRepository,
			ExpenseRepository expenseRepository,
			SettlementCalculator settlementCalculator,
			ServiceChargeCalculator serviceChargeCalculator) {
		this.groupRepository = groupRepository;
		this.expenseRepository = expenseRepository;
		this.settlementCalculator = settlementCalculator;
		this.serviceChargeCalculator = serviceChargeCalculator;
	}

	@Transactional(readOnly = true)
	public SettlementResponse calculate(UUID groupId) {
		BillGroup group = groupRepository.findWithParticipantsById(groupId)
				.orElseThrow(() -> new ResourceNotFoundException("Group " + groupId + " was not found"));
		List<Expense> expenses = expenseRepository.findAllByGroupIdOrderByCreatedAtAsc(groupId);

		List<SettlementCalculator.ParticipantSnapshot> participants = group.getParticipants().stream()
				.map(participant -> new SettlementCalculator.ParticipantSnapshot(
						participant.getId(), participant.getName()))
				.toList();
		List<SettlementCalculator.ExpenseSnapshot> expenseSnapshots = expenses.stream()
				.map(this::toSnapshot)
				.toList();
		SettlementCalculator.SettlementCalculation calculation =
				settlementCalculator.calculate(participants, expenseSnapshots);

		return new SettlementResponse(
				groupId,
				calculation.totalExpenses(),
				serviceChargeCalculator.percentage(),
				serviceChargeCalculator.amountFor(calculation.totalExpenses()),
				calculation.balances(),
				calculation.settlements());
	}

	private SettlementCalculator.ExpenseSnapshot toSnapshot(Expense expense) {
		List<SettlementCalculator.ShareSnapshot> shares = expense.getShares().stream()
				.map(this::toSnapshot)
				.toList();
		return new SettlementCalculator.ExpenseSnapshot(
				expense.getPayer().getId(), expense.getAmount(), shares);
	}

	private SettlementCalculator.ShareSnapshot toSnapshot(ExpenseShare share) {
		return new SettlementCalculator.ShareSnapshot(share.getParticipant().getId(), share.getAmount());
	}
}
