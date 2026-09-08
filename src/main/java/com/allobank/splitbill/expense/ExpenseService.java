package com.allobank.splitbill.expense;

import com.allobank.splitbill.common.DomainValidationException;
import com.allobank.splitbill.common.ResourceNotFoundException;
import com.allobank.splitbill.group.BillGroup;
import com.allobank.splitbill.group.BillGroupRepository;
import com.allobank.splitbill.group.Participant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

	private final BillGroupRepository groupRepository;
	private final ExpenseRepository expenseRepository;
	private final EqualSplitAllocator equalSplitAllocator;

	public ExpenseService(
			BillGroupRepository groupRepository,
			ExpenseRepository expenseRepository,
			EqualSplitAllocator equalSplitAllocator) {
		this.groupRepository = groupRepository;
		this.expenseRepository = expenseRepository;
		this.equalSplitAllocator = equalSplitAllocator;
	}

	@Transactional
	public ExpenseResponse create(UUID groupId, CreateExpenseRequest request) {
		BillGroup group = groupRepository.findWithParticipantsById(groupId)
				.orElseThrow(() -> new ResourceNotFoundException("Group " + groupId + " was not found"));

		Map<UUID, Participant> participants = group.getParticipants().stream()
				.collect(Collectors.toMap(Participant::getId, Function.identity()));
		Participant payer = requireMember(request.paidByParticipantId(), participants, "Payer");
		List<Participant> beneficiaries = resolveBeneficiaries(request.beneficiaryParticipantIds(), participants);

		if (request.amount().compareTo(BigDecimal.valueOf(beneficiaries.size())) < 0) {
			throw new DomainValidationException("Amount must be at least the number of beneficiaries");
		}

		Expense expense = new Expense(group, payer, request.description().trim(), request.amount());
		allocateShares(expense, beneficiaries, request.amount());
		return ExpenseResponse.from(expenseRepository.save(expense));
	}

	private List<Participant> resolveBeneficiaries(List<UUID> ids, Map<UUID, Participant> participants) {
		Set<UUID> uniqueIds = new HashSet<>();
		Map<UUID, Participant> ordered = new LinkedHashMap<>();
		for (UUID id : ids) {
			if (!uniqueIds.add(id)) {
				throw new DomainValidationException("Beneficiary participant IDs must be unique");
			}
			ordered.put(id, requireMember(id, participants, "Beneficiary"));
		}
		return List.copyOf(ordered.values());
	}

	private Participant requireMember(UUID id, Map<UUID, Participant> participants, String role) {
		Participant participant = participants.get(id);
		if (participant == null) {
			throw new DomainValidationException(role + " must be a participant of the requested group");
		}
		return participant;
	}

	private void allocateShares(Expense expense, List<Participant> beneficiaries, BigDecimal amount) {
		List<BigDecimal> shares = equalSplitAllocator.allocate(amount, beneficiaries.size());
		for (int index = 0; index < beneficiaries.size(); index++) {
			expense.addShare(beneficiaries.get(index), shares.get(index), index);
		}
	}
}
