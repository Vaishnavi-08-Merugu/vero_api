package com.vero.api.repository;

import com.vero.api.model.Category;
import com.vero.api.model.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    /**
     * Get all transactions for an account
     */
    List<Transaction> findByAccountId(Long accountId);



    /**
     * Get transactions by category
     */
    List<Transaction> findByCategory(Category category);



    /**
     * Get transactions between two dates
     */
    List<Transaction> findByTransactionDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );



    /**
     * Get transactions by category and month.
     * Used by calculateMonthlySpend()
     */
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.category = :category
            AND YEAR(t.transactionDate) = :year
            AND MONTH(t.transactionDate) = :month
            """)
    List<Transaction> findByCategoryAndMonth(
            @Param("category") Category category,
            @Param("year") int year,
            @Param("month") int month
    );

}