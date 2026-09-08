package com.allobank.splitbill.group;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGroupRequest(
		@NotBlank @Size(max = 100) String name,
		@NotNull @Size(min = 2) List<@Valid ParticipantRequest> participants) {

	public record ParticipantRequest(@NotBlank @Size(max = 100) String name) {
	}
}
