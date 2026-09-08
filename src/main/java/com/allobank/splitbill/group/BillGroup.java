package com.allobank.splitbill.group;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bill_groups")
public class BillGroup {

	@Id
	private UUID id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Participant> participants = new ArrayList<>();

	protected BillGroup() {
	}

	public BillGroup(String name, List<String> participantNames) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.createdAt = Instant.now();
		participantNames.forEach(participantName -> participants.add(new Participant(this, participantName)));
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public List<Participant> getParticipants() {
		return Collections.unmodifiableList(participants);
	}
}
