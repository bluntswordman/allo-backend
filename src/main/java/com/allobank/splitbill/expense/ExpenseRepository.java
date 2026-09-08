package com.allobank.splitbill.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

	@Query("""
			select distinct expense from Expense expense
			join fetch expense.payer
			left join fetch expense.shares share
			left join fetch share.participant
			where expense.group.id = :groupId
			order by expense.createdAt asc
			""")
	List<Expense> findAllByGroupIdOrderByCreatedAtAsc(@Param("groupId") UUID groupId);
}
