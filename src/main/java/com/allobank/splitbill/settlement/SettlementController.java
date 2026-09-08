package com.allobank.splitbill.settlement;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/settlements")
public class SettlementController {

	private final SettlementService settlementService;

	public SettlementController(SettlementService settlementService) {
		this.settlementService = settlementService;
	}

	@GetMapping
	SettlementResponse calculate(@PathVariable UUID groupId) {
		return settlementService.calculate(groupId);
	}
}
