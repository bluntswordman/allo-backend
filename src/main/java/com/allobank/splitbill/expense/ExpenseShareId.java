package com.allobank.splitbill.expense;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ExpenseShareId implements Serializable {

	private UUID expense;
	private UUID participant;

	public ExpenseShareId() {
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ExpenseShareId that)) {
			return false;
		}
		return Objects.equals(expense, that.expense) && Objects.equals(participant, that.participant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expense, participant);
	}
}
