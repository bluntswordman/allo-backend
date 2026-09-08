package com.allobank.splitbill.expense;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
public class ExpenseController {

	private final ExpenseService expenseService;

	public ExpenseController(ExpenseService expenseService) {
		this.expenseService = expenseService;
	}

	@PostMapping
	ResponseEntity<ExpenseResponse> create(
			@PathVariable UUID groupId,
			@Valid @RequestBody CreateExpenseRequest request) {
		ExpenseResponse response = expenseService.create(groupId, request);
		URI location = URI.create("/api/v1/groups/" + groupId + "/expenses/" + response.id());
		return ResponseEntity.created(location).body(response);
	}
}
