package com.allobank.splitbill.group;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

	private final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

	@PostMapping
	ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest request) {
		GroupResponse response = groupService.create(request);
		return ResponseEntity.created(URI.create("/api/v1/groups/" + response.id())).body(response);
	}
}
