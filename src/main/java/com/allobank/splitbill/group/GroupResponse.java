package com.allobank.splitbill.group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupResponse(UUID id, String name, List<ParticipantResponse> participants, Instant createdAt) {

	public record ParticipantResponse(UUID id, String name, Instant createdAt) {
	}

	static GroupResponse from(BillGroup group) {
		List<ParticipantResponse> participants = group.getParticipants().stream()
				.map(participant -> new ParticipantResponse(
						participant.getId(), participant.getName(), participant.getCreatedAt()))
				.toList();
		return new GroupResponse(group.getId(), group.getName(), participants, group.getCreatedAt());
	}
}
