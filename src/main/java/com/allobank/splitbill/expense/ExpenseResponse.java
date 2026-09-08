package com.allobank.splitbill.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExpenseResponse(
		UUID id,
		UUID groupId,
		String description,
		BigDecimal amount,
		ParticipantSummary payer,
		List<ShareResponse> shares,
		Instant createdAt) {

	public record ParticipantSummary(UUID id, String name) {
	}

	public record ShareResponse(UUID participantId, String participantName, BigDecimal amount) {
	}

	static ExpenseResponse from(Expense expense) {
		List<ShareResponse> shares = expense.getShares().stream()
				.map(share -> new ShareResponse(share.getParticipant().getId(),
						share.getParticipant().getName(), share.getAmount()))
				.toList();
		return new ExpenseResponse(
				expense.getId(),
				expense.getGroupId(),
				expense.getDescription(),
				expense.getAmount(),
				new ParticipantSummary(expense.getPayer().getId(), expense.getPayer().getName()),
				shares,
				expense.getCreatedAt());
	}
}
