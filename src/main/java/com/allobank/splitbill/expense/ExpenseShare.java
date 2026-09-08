package com.allobank.splitbill.expense;

import com.allobank.splitbill.group.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_shares")
@IdClass(ExpenseShareId.class)
public class ExpenseShare {

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "expense_id", nullable = false)
	private Expense expense;

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "participant_id", nullable = false)
	private Participant participant;

	@Column(name = "share_amount", nullable = false, precision = 19, scale = 0)
	private BigDecimal amount;

	@Column(name = "allocation_order", nullable = false)
	private int allocationOrder;

	protected ExpenseShare() {
	}

	ExpenseShare(Expense expense, Participant participant, BigDecimal amount, int allocationOrder) {
		this.expense = expense;
		this.participant = participant;
		this.amount = amount;
		this.allocationOrder = allocationOrder;
	}

	public Participant getParticipant() {
		return participant;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public int getAllocationOrder() {
		return allocationOrder;
	}
}
