package com.allobank.splitbill.group;

import com.allobank.splitbill.common.DomainValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class GroupService {

	private final BillGroupRepository groupRepository;

	public GroupService(BillGroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	@Transactional
	public GroupResponse create(CreateGroupRequest request) {
		String groupName = request.name().trim();
		List<String> participantNames = request.participants().stream()
				.map(participant -> participant.name().trim())
				.toList();
		validateUniqueNames(participantNames);

		BillGroup group = groupRepository.save(new BillGroup(groupName, participantNames));
		return GroupResponse.from(group);
	}

	private void validateUniqueNames(List<String> names) {
		Set<String> normalizedNames = new HashSet<>();
		for (String name : names) {
			if (!normalizedNames.add(name.toLowerCase(Locale.ROOT))) {
				throw new DomainValidationException("Participant names must be unique within a group");
			}
		}
	}
}
