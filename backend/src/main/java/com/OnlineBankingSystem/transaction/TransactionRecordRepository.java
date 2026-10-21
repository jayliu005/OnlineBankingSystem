package com.OnlineBankingSystem.transaction;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

	@Query("""
			select t from TransactionRecord t
			where (t.accountFrom.id = :accountId or t.accountTo.id = :accountId)
			and t.transactionTime >= :startTime
			and t.transactionTime < :endTime
			order by t.transactionTime asc, t.id asc
			""")
	List<TransactionRecord> findForAccountAndTimeRange(
			@Param("accountId") Long accountId,
			@Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime);
}
