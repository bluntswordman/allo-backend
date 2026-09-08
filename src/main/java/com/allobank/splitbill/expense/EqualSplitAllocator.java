package com.allobank.splitbill.expense;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class EqualSplitAllocator {

	public List<BigDecimal> allocate(BigDecimal amount, int participantCount) {
		if (participantCount <= 0) {
			throw new IllegalArgumentException("Participant count must be positive");
		}
		if (amount.signum() <= 0 || amount.scale() > 0) {
			throw new IllegalArgumentException("Amount must be a positive whole number");
		}
		if (amount.compareTo(BigDecimal.valueOf(participantCount)) < 0) {
			throw new IllegalArgumentException("Amount must be at least the participant count");
		}

		BigDecimal[] division = amount.divideAndRemainder(BigDecimal.valueOf(participantCount));
		BigDecimal baseShare = division[0];
		int remainder = division[1].intValueExact();
		List<BigDecimal> shares = new ArrayList<>(participantCount);
		for (int index = 0; index < participantCount; index++) {
			shares.add(index < remainder ? baseShare.add(BigDecimal.ONE) : baseShare);
		}
		return List.copyOf(shares);
	}
}
