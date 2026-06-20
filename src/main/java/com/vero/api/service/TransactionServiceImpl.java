package com.vero.api.service;

import com.vero.api.dto.TransactionRequest;
import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import com.vero.api.repository.TransactionRepository;
import com.vero.api.util.BudgetCalculator;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class TransactionServiceImpl implements TransactionService {


    private final TransactionRepository repository;


    public TransactionServiceImpl(TransactionRepository repository) {
        this.repository = repository;
    }



    @Override
    public List<Transaction> getAllTransactions() {

        return repository.findAll();

    }



    @Override
    public Optional<Transaction> getTransactionById(Long id) {

        return repository.findById(id);

    }



    @Override
    public List<Transaction> getTransactionsByAccount(Long accountId) {

        return repository.findByAccountId(accountId);

    }



    @Override
    public List<Transaction> getTransactionsByDateRange(
            LocalDate startDate,
            LocalDate endDate) {


        return repository.findAll()
                .stream()
                .filter(transaction -> {

                    LocalDate date =
                            transaction.getTransactionDate();

                    return !date.isBefore(startDate)
                            &&
                            !date.isAfter(endDate);

                })
                .toList();

    }



    @Override
    public Transaction createTransaction(TransactionRequest request) {


        Transaction transaction = new Transaction();


        transaction.setAccountId(request.getAccountId());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(request.getCategory());
        transaction.setTransactionDate(request.getTransactionDate());


        return repository.save(transaction);

    }




    @Override
    public void deleteTransaction(Long id) {

        repository.deleteById(id);

    }





    /**
     * Monthly spend grouped by category.
     *
     * Includes first day of month.
     */
    @Override
    public Map<Category, BigDecimal> calculateMonthlySpend(
            int year,
            int month) {


        LocalDate startDate =
                LocalDate.of(year, month, 1);


        LocalDate endDate =
                startDate.withDayOfMonth(
                        startDate.lengthOfMonth()
                );


        return repository.findAll()
                .stream()
                .filter(transaction -> {

                    LocalDate date =
                            transaction.getTransactionDate();


                    return !date.isBefore(startDate)
                            &&
                            !date.isAfter(endDate);

                })
                .collect(
                        java.util.stream.Collectors.groupingBy(
                                Transaction::getCategory,
                                java.util.stream.Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Transaction::getAmount,
                                        BigDecimal::add
                                )
                        )
                );

    }




    @Override
    public Map<Category, BigDecimal> getTopSpendingCategories(
            List<Transaction> transactions,
            int topN) {


        return BudgetCalculator
                .getTopSpendingCategories(
                        transactions,
                        topN
                );

    }


}