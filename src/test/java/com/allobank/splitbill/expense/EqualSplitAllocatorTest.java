package com.allobank.splitbill.expense;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class EqualSplitAllocatorTest {

	private final EqualSplitAllocator allocator = new EqualSplitAllocator();

	@Test
	void distributesRemainderInParticipantOrder() {
		List<BigDecimal> shares = allocator.allocate(new BigDecimal("100"), 3);

		assertThat(shares).containsExactly(
				new BigDecimal("34"), new BigDecimal("33"), new BigDecimal("33"));
		assertThat(shares.stream().reduce(BigDecimal.ZERO, BigDecimal::add))
				.isEqualByComparingTo("100");
	}

	@Test
	void splitsEvenAmountEqually() {
		assertThat(allocator.allocate(new BigDecimal("300"), 3))
				.containsExactly(new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"));
	}

	@Test
	void rejectsAmountThatWouldCreateZeroShare() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> allocator.allocate(BigDecimal.ONE, 2));
	}

	@Test
	void rejectsFractionalAmount() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> allocator.allocate(new BigDecimal("10.5"), 2));
	}
}
