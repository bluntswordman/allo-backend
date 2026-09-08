package com.allobank.splitbill.settlement;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SettlementResponse(
		UUID groupId,
		BigDecimal totalExpenses,
		int serviceChargePct,
		BigDecimal serviceChargeAmount,
		List<SettlementCalculator.ParticipantBalance> balances,
		List<SettlementCalculator.Transfer> settlements) {
}
