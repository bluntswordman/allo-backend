package com.allobank.splitbill.settlement;

import com.allobank.splitbill.common.SplitBillProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Component
public class ServiceChargeCalculator {

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	private final int percentage;

	public ServiceChargeCalculator(SplitBillProperties properties) {
		this.percentage = percentageFor(properties.githubUsername());
	}

	public int percentage() {
		return percentage;
	}

	public BigDecimal amountFor(BigDecimal totalExpenses) {
		return totalExpenses.multiply(BigDecimal.valueOf(percentage))
				.divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP);
	}

	static int percentageFor(String username) {
		int characterSum = username.toLowerCase(Locale.ROOT).codePoints().sum();
		return characterSum % 10;
	}
}
