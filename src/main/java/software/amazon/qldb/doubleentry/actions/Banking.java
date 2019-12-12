/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package software.amazon.qldb.doubleentry.actions;

import com.amazon.ion.Decimal;
import com.amazon.ion.IonStruct;
import com.amazon.ion.IonValue;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import software.amazon.qldb.Result;
import software.amazon.qldb.TransactionExecutor;
import software.amazon.qldb.doubleentry.Constants;
import software.amazon.qldb.doubleentry.helpers.IonHelper;
import software.amazon.qldb.doubleentry.helpers.TransactionsHandler;
import software.amazon.qldb.doubleentry.models.Balance;
import software.amazon.qldb.doubleentry.models.Transaction;
import software.amazon.qldb.doubleentry.models.TransactionEntry;
import software.amazon.qldb.doubleentry.models.TransactionType;
import software.amazon.qldb.doubleentry.models.TransferRequest;
import software.amazon.qldb.doubleentry.models.TransferResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * The purpose of this class is to encapsulate all the banking actions
 * like checking the balances, * transferring the money etc.
 * </p>
 *
 * <p>
 * We inject the TransactionHandler, which manages the interaction with
 * QLDB driver.
 * </p>
 */
@Slf4j
public class Banking {

    private TransactionsHandler transactionsHandler;

    private IonHelper ionHelper;

    public Banking(@NonNull final TransactionsHandler transactionsHandler,
                   @NonNull final IonHelper ionHelper) {
        this.transactionsHandler = transactionsHandler;
        this.ionHelper = ionHelper;
    }

    /**
     * Get the balances for the given AccountId. This method is intended to
     * be used from the example code and just  logs the balance.
     *
     * @param accountId The AccountId to get the balances for
     */
    public List<Balance> getBalancesForAccount(@NonNull final String accountId) {
        return transactionsHandler.executeTransaction(txn -> {
            final List<Balance> balances = this.getBalancesForAccount(txn, accountId);
            return balances;
        }, (retry) -> log.info("There was an error while checking for balance. Retrying "));
    }

    /**
     * <p>
     * Initiate the transfer of money between two accounts. The method first
     * validates all the parameters, and then starts a QLDB Transaction(via the
     * TransactionHandler). In the QLDB Transaction we do the following steps:
     * </p>
     * <ol>
     *     <li>Read the Balances of the Sender Account </li>
     *     <li>Read the Balances of the Receiver Account </li>
     *     <li> Check if Sender Account and Receiver Accounts support the currency
     *     and Sender Account has balance more than the requested transfer amount
     *     </li>
     *     <li>Once the above checks pass, we create an entry
     *     in the Transactions table </li>
     *     <li>Calculate and update the balance of Sender Account</li>
     *     <li>Calculate and update the balance of Receiver Account</li>
     * </ol>
     *
     * <p>
     * All the above mentioned steps are part of a single QLDB Transaction.
     * If there is an OCC while committing this transaction, then the
     * QLDB Driver(or specifically, QLDB session) takes care of retrying the
     * entire transaction, meaning, the failed transaction will start again from
     * reading the balances, doing the business validations again with the new
     * values, and then updating the balance to the correct values.
     * </p>
     */
    public TransferResponse transfer(@NonNull final TransferRequest transferRequest) {

        //Validate that the input parameters are correct
        validateParameters(transferRequest);

        /*
        * The executeTransaction Method of TransactionsHandler will take care
        * of getting the QLDB session and executing  the given transaction
        * body(via the anonymous function)
        *
        * If there is an OCC while doing the transaction, this entire anonymous
        * function will be tried again, meaning, the balances will be read
        * again, the balance checks will be done again and the new balances
        * will be computed  and the transaction commit will be tried again.
        *
        */

        final String senderAccountId = transferRequest.getSenderAccountId();
        final String receiverAccountId = transferRequest.getReceiverAccountId();
        final String currency = transferRequest.getCurrency();
        final double amount = transferRequest.getAmount();

        /*
        * transferSuccessful flag should default to false unless we actually
        * mark it to true when Transfer is done successfully
        */
        final TransferResponse response = TransferResponse.builder()
                .transferSuccessful(false)
                .build();

        return transactionsHandler.executeTransaction(txn -> {
            final List<Balance> senderAccountBalances =
                    getBalancesForAccount(txn, senderAccountId);

            log.debug("The Balance for AccountId {} is {}",
                    senderAccountId, senderAccountBalances);

            final List<Balance> receiverAccountBalances =
                    getBalancesForAccount(txn, receiverAccountId);

            log.debug("The Balance for AccountId {} is {}",
                    receiverAccountId, receiverAccountBalances);

            if (senderHasSufficientBalance(senderAccountBalances, currency, amount) &&
                    receiverAcceptsCurrency(receiverAccountBalances, currency)) {

                addEntryInTransactions(txn, transferRequest);

                updateBalance(txn, senderAccountBalances, senderAccountId,
                        currency, amount, TransactionType.DEBIT);

                updateBalance(txn, receiverAccountBalances, receiverAccountId,
                        currency, amount, TransactionType.CREDIT);
                response.setTransferSuccessful(true);
                response.setUpdatedReceiverBalances(receiverAccountBalances);
                response.setUpdatedSenderBalances(senderAccountBalances);
                return response;
            }
            return response;
        }, (retry) -> log.info("There was an error "));
    }


    private void validateParameters(
            @NonNull final TransferRequest transferRequest) {

        Validate.isTrue(transferRequest.getAmount() > 0);
        Validate.notBlank(transferRequest.getSenderAccountId());
        Validate.notBlank(transferRequest.getReceiverAccountId());
        Validate.isTrue(Constants.SUPPORTED_CURRENCIES.contains(transferRequest.getCurrency()));
        Validate.isTrue(!transferRequest.getSenderAccountId().equals(transferRequest.getReceiverAccountId()));
    }

    /**
     * Given an AccountId, get all the balances of the account
     * This method is called as a part of the QLDB Transaction and takes in the
     * TransactionExecutor instance as an input  argument.
     *
     * @param txn The TransactionExecutor object which is instantiated during
     *            the QLDB Transaction
     * @param accountId
     * @return List of Balances for the given AccountId
     */
    private List<Balance> getBalancesForAccount(
            @NonNull final TransactionExecutor txn,
            @NonNull final String accountId) {

        List<Balance> balances = new ArrayList<>();

        final String queryString = "SELECT Balances FROM Accounts WHERE AccountId = ?";
        final List<IonValue> parameters = Collections.singletonList(
                ionHelper.toIonValue(accountId));

        log.debug("Reading the balance for AccountID {}", accountId);
        final Result result = txn.execute(queryString, parameters);
        if (result.isEmpty()) {
            log.error("Could not find any balances for the account {}", result);
            return balances;
        }

        final List<IonStruct> documents = ionHelper.toIonStructs(result);
        if (1 != documents.size()) {
            log.error("More than one accounts exist for the same Account Id {}. Cannot decide which account to "
                    + "pick", accountId);
            return balances;
        }

        balances = Arrays.asList(ionHelper.readIonValue(documents.get(0).get("Balances"), Balance[].class));
        return balances;
    }

    /**
     * Create an entry in the Transaction Table. This creates a single document
     * in the transactions table
     *
     * @param txn The TransactionExecutor object which is instantiated during
     *            the QLDB Transaction
     * @param transferRequest
     * @return List of documentIds created in the transactions table
     */
    private List<String> addEntryInTransactions(
            @NonNull final TransactionExecutor txn,
            @NonNull final TransferRequest transferRequest) {

        final TransactionEntry senderTransactionEntry =
                TransactionEntry.builder()
                .accountId(transferRequest.getSenderAccountId())
                .transactionType(TransactionType.DEBIT.name())
                .notes(transferRequest.getNotes())
                .amount(Decimal.valueOf(transferRequest.getAmount()))
                .currency(transferRequest.getCurrency())
                .build();

        final TransactionEntry receiverTransactionEntry =
                TransactionEntry.builder()
                .accountId(transferRequest.getReceiverAccountId())
                .transactionType(TransactionType.CREDIT.name())
                .notes(transferRequest.getNotes())
                .amount(Decimal.valueOf(transferRequest.getAmount()))
                .currency(transferRequest.getCurrency())
                .build();

        final Transaction transaction = Transaction.builder()
                .transactionTime(LocalDate.now())
                .senderAccountEntry(senderTransactionEntry)
                .receiverAccountEntry(receiverTransactionEntry)
                .build();

        final String query = "INSERT INTO Transactions VALUE ? ";
        final IonValue transactionDocument =
                ionHelper.toIonValue(transaction);

        final List<IonValue> parameters =
                Collections.singletonList(transactionDocument);

        final Result result = txn.execute(query, parameters);
        final List<String> insertedDocumentIds = ionHelper.getDocumentIdsFromDmlResult(result);

        log.info("Created entries in Transactions table. " +
                "Inserted document ids {}", insertedDocumentIds);

        return insertedDocumentIds;
    }

    /**
     * Update the balance, of a particular currency, for the given AccountId.
     * This method computes the balance to be updated and
     * then executes the "UPDATE" Query
     *
     * @param txn The TransactionExecutor object which is instantiated during
     *            the QLDB Transaction
     * @param balances
     * @param accountId
     * @param currency
     * @param amount
     * @param transactionType
     * @return List of modified documents in the Accounts table
     */
    private List<String> updateBalance(@NonNull final TransactionExecutor txn,
                                       @NonNull final List<Balance> balances,
                                       @NonNull final String accountId,
                                       @NonNull final String currency,
                                       final double amount,
                                       final TransactionType transactionType) {

        final List<Balance> updatedCurrencyBalances =
                updateBalanceForCurrency(balances, currency, amount,
                        transactionType);

        final String query = "UPDATE Accounts SET Balances = ? WHERE AccountId = ?";

        final List<IonValue> parameters = new ArrayList<>();
        parameters.add(ionHelper.toIonValue(updatedCurrencyBalances));
        parameters.add(ionHelper.toIonValue(accountId));

        final Result result = txn.execute(query, parameters);
        final List<String> insertedDocumentIds = ionHelper.getDocumentIdsFromDmlResult(result);

        log.info("Updated entries in Accounts table for Account Id {}. Affected document ids are {}",
                accountId, insertedDocumentIds);
        return insertedDocumentIds;
    }

    /**
     * Check if the sender account has enough balance for the given currency
     */
    private Boolean senderHasSufficientBalance(@NonNull final List<Balance> senderAccountBalances,
                                         @NonNull final String currency,
                                         final double amount) {

        final Optional<Balance> senderAccountCurrencyBalance = getBalanceForCurrency(senderAccountBalances, currency);

        final Boolean senderSatisfiesCondition = senderAccountCurrencyBalance
                        .map(Balance::getCurrencyBalance)
                        .filter(b -> (b.doubleValue() > amount))
                        .isPresent();

        return senderSatisfiesCondition;
    }

    /**
     * Check if receiver account accepts the currency
     */
    private Boolean receiverAcceptsCurrency(@NonNull final List<Balance> receiverAccountBalances,
                                            final String currency) {
        final Optional<Balance> receiverAccountCurrencyBalance = getBalanceForCurrency(receiverAccountBalances, currency);
        return receiverAccountCurrencyBalance.isPresent();
    }

    /**
     * Given a currency and list of Balance objects, where each object has two
     * attributes: currency & balance, get the Balance of input currency.
     */
    private Optional<Balance> getBalanceForCurrency(@NonNull final List<Balance> balances,
                                                    @NonNull final String currency) {
        return balances.stream()
                .filter(b -> currency.equals(b.getCurrency()))
                .findFirst();
    }

    /**
     * Update the Balance of the given currency by the given input amount.
     * The update can be either an addition or subtraction depending on the
     * TransactionType.
     *
     * Note: This method just modifies the Balance Object in the list but does
     * not write anything to the DB
     */
    private List<Balance> updateBalanceForCurrency(@NonNull final List<Balance> balances,
                                                   @NonNull final String currency,
                                                   final double amount,
                                                   final TransactionType transactionType) {
        balances.forEach(balance -> {
            if(currency.equals(balance.getCurrency())) {
                if (TransactionType.DEBIT.equals(transactionType)) {
                    balance.setCurrencyBalance(
                            Decimal.valueOf(balance.getCurrencyBalance().subtract(Decimal.valueOf(amount))));
                } else if (TransactionType.CREDIT.equals(transactionType)) {
                    balance.setCurrencyBalance(
                            Decimal.valueOf(balance.getCurrencyBalance().add(Decimal.valueOf(amount))));
                }
            }
        });
        return balances;
    }

}
