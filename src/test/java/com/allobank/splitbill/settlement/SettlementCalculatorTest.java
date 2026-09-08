package com.allobank.splitbill.settlement;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class SettlementCalculatorTest {

	private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");
	private static final UUID CAROL = UUID.fromString("00000000-0000-0000-0000-000000000003");

	private final SettlementCalculator calculator = new SettlementCalculator();

	@Test
	void settlesOnePayerExpenseForWholeGroup() {
		SettlementCalculator.SettlementCalculation result = calculator.calculate(participants(), List.of(
				expense(ALICE, "300", share(ALICE, "100"), share(BOB, "100"), share(CAROL, "100"))));

		assertThat(result.totalExpenses()).isEqualByComparingTo("300");
		assertThat(result.settlements()).containsExactly(
				new SettlementCalculator.Transfer(BOB, "Bob", ALICE, "Alice", new BigDecimal("100")),
				new SettlementCalculator.Transfer(CAROL, "Carol", ALICE, "Alice", new BigDecimal("100")));
		assertThat(result.balances()).extracting(SettlementCalculator.ParticipantBalance::netBalance)
				.containsExactly(new BigDecimal("200"), new BigDecimal("-100"), new BigDecimal("-100"));
	}

	@Test
	void netsMultipleExpensesBeforeCreatingTransfers() {
		SettlementCalculator.SettlementCalculation result = calculator.calculate(participants(), List.of(
				expense(ALICE, "120", share(ALICE, "40"), share(BOB, "40"), share(CAROL, "40")),
				expense(BOB, "60", share(ALICE, "30"), share(BOB, "30"))));

		assertThat(result.settlements()).containsExactly(
				new SettlementCalculator.Transfer(CAROL, "Carol", ALICE, "Alice", new BigDecimal("40")),
				new SettlementCalculator.Transfer(BOB, "Bob", ALICE, "Alice", new BigDecimal("10")));
	}

	@Test
	void supportsPayerWhoIsNotABeneficiary() {
		SettlementCalculator.SettlementCalculation result = calculator.calculate(participants(), List.of(
				expense(ALICE, "100", share(BOB, "50"), share(CAROL, "50"))));

		assertThat(result.settlements()).containsExactly(
				new SettlementCalculator.Transfer(BOB, "Bob", ALICE, "Alice", new BigDecimal("50")),
				new SettlementCalculator.Transfer(CAROL, "Carol", ALICE, "Alice", new BigDecimal("50")));
	}

	@Test
	void returnsNoTransfersWhenThereAreNoExpenses() {
		SettlementCalculator.SettlementCalculation result = calculator.calculate(participants(), List.of());

		assertThat(result.totalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.settlements()).isEmpty();
		assertThat(result.balances()).allMatch(balance -> balance.netBalance().signum() == 0);
	}

	@Test
	void rejectsCorruptedShareTotals() {
		assertThatIllegalStateException().isThrownBy(() -> calculator.calculate(participants(), List.of(
				expense(ALICE, "100", share(ALICE, "40"), share(BOB, "40")))));
	}

	private List<SettlementCalculator.ParticipantSnapshot> participants() {
		return List.of(
				new SettlementCalculator.ParticipantSnapshot(ALICE, "Alice"),
				new SettlementCalculator.ParticipantSnapshot(BOB, "Bob"),
				new SettlementCalculator.ParticipantSnapshot(CAROL, "Carol"));
	}

	private SettlementCalculator.ExpenseSnapshot expense(
			UUID payerId, String amount, SettlementCalculator.ShareSnapshot... shares) {
		return new SettlementCalculator.ExpenseSnapshot(
				payerId, new BigDecimal(amount), List.of(shares));
	}

	private SettlementCalculator.ShareSnapshot share(UUID participantId, String amount) {
		return new SettlementCalculator.ShareSnapshot(participantId, new BigDecimal(amount));
	}
}
