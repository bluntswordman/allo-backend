package com.allobank.splitbill.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participants")
public class Participant {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private BillGroup group;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected Participant() {
	}

	Participant(BillGroup group, String name) {
		this.id = UUID.randomUUID();
		this.group = group;
		this.name = name;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getGroupId() {
		return group.getId();
	}

	public String getName() {
		return name;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
