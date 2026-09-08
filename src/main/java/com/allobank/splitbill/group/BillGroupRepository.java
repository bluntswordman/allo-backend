package com.allobank.splitbill.group;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BillGroupRepository extends JpaRepository<BillGroup, UUID> {

	@EntityGraph(attributePaths = "participants")
	Optional<BillGroup> findWithParticipantsById(UUID id);
}
