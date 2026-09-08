package com.allobank.splitbill.settlement;

import com.allobank.splitbill.common.SplitBillProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceChargeCalculatorTest {

	@Test
	void computesPercentageCaseInsensitivelyFromUsername() {
		assertThat(ServiceChargeCalculator.percentageFor("bluntswordman")).isEqualTo(4);
		assertThat(ServiceChargeCalculator.percentageFor("BLUNTSWORDMAN")).isEqualTo(4);
	}

	@Test
	void roundsServiceChargeToWholeRupiahUsingHalfUp() {
		ServiceChargeCalculator calculator =
				new ServiceChargeCalculator(new SplitBillProperties("bluntswordman"));

		assertThat(calculator.amountFor(new BigDecimal("13"))).isEqualByComparingTo("1");
		assertThat(calculator.amountFor(new BigDecimal("100000"))).isEqualByComparingTo("4000");
	}
}
