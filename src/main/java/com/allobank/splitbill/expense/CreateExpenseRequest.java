package com.allobank.splitbill.expense;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateExpenseRequest(
		@NotBlank @Size(max = 255) String description,
		@NotNull @Positive @Digits(integer = 18, fraction = 0) BigDecimal amount,
		@NotNull UUID paidByParticipantId,
		@NotEmpty List<@NotNull UUID> beneficiaryParticipantIds) {
}
