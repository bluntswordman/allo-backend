package com.allobank.splitbill.expense;

import com.allobank.splitbill.group.BillGroup;
import com.allobank.splitbill.group.Participant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class Expense {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private BillGroup group;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "payer_id", nullable = false)
	private Participant payer;

	@Column(nullable = false, length = 255)
	private String description;

	@Column(nullable = false, precision = 19, scale = 0)
	private BigDecimal amount;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("allocationOrder ASC")
	private List<ExpenseShare> shares = new ArrayList<>();

	protected Expense() {
	}

	public Expense(BillGroup group, Participant payer, String description, BigDecimal amount) {
		this.id = UUID.randomUUID();
		this.group = group;
		this.payer = payer;
		this.description = description;
		this.amount = amount;
		this.createdAt = Instant.now();
	}

	public void addShare(Participant participant, BigDecimal amount, int allocationOrder) {
		shares.add(new ExpenseShare(this, participant, amount, allocationOrder));
	}

	public UUID getId() {
		return id;
	}

	public UUID getGroupId() {
		return group.getId();
	}

	public Participant getPayer() {
		return payer;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public List<ExpenseShare> getShares() {
		return Collections.unmodifiableList(shares);
	}
}
