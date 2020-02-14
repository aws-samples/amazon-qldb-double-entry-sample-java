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

package software.amazon.qldb.doubleentry.tasks;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.qldb.doubleentry.actions.Banking;
import software.amazon.qldb.doubleentry.dagger.components.BankingComponent;
import software.amazon.qldb.doubleentry.dagger.components.DaggerBankingComponent;
import software.amazon.qldb.doubleentry.models.Balance;
import software.amazon.qldb.doubleentry.models.TransferRequest;
import software.amazon.qldb.doubleentry.models.TransferResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * This class illustrates the use case of transferring the money between
 * two accounts. We show case two cases in this:
 * </p>
 *
 * <ul>
 *     <li> Only one transfer of money happening at a time </li>
 *     <li> Multiple transfers between same accounts happening in parallel </li>
 * </ul>
 *
 * <p>
 * In the first use case, we will do only one transfer at a time and assume that
 * there is no other
 * conflicting transaction happening in the system.
 *
 * In the second use case, we will spawn multiple threads each doing a
 * transaction of its own but with the same accounts involved.
 * In doing so, some threads will get an OCC error, and they will retry their
 * transaction and eventually succeed. Once all the threads complete,
 * we ensure that the balance across accounts is as expected
 * </p>
 *
 * <p>
 * In these use cases we leverage the ".execute" method of QLDB Java driver.
 * This is a convenience method which takes care of starting the transaction,
 * executing the statements, and  committing the transaction.
 * If, during the commit phase, there is an OCC exception, the method will retry
 * the entire transaction again (till it has exhausted the maximum retry
 * attempts or it times out). With this, the application  does not have to worry
 * about doing the retries manually.
 * </p>
 */
@Slf4j
public class TransferMoney {

    private Banking banking;


    public TransferMoney(@NonNull final Banking banking) {
        this.banking = banking;
    }

    /**
     * This shows transfer of 5000 USD from Account A001 to Account A003
     * We log the balances of Accounts A001 and A003 before and after the
     * transfer.
     *
     * After the Transfer:
     * 1) The USD balance in account A001 should be $5000 less than what we
     * logged initially
     *
     * 2) The USD balance in account A003 should be $5000 more than what we
     * logged initially
     */
    public void runSingleTransfer() {
        final List<Balance> balancesForA001BeforeTransfer = banking.getBalancesForAccount("A001");
        log.info("Balances for Account A001 before Transfer {}", balancesForA001BeforeTransfer);

        final List<Balance> balancesForA003BeforeTransfer = banking.getBalancesForAccount("A003");
        log.info("Balances for Account A003 before Transfer {}", balancesForA003BeforeTransfer);

        final TransferResponse transferResponse = banking.transfer(
                TransferRequest.builder()
                .senderAccountId("A001")
                .receiverAccountId("A003")
                .currency("USD")
                .amount(5000)
                .notes("Fixed Fee")
                .build()
        );

        final List<Balance> balancesForA001AfterTransfer = banking.getBalancesForAccount("A001");
        log.info("Balances for Account A001 after Transfer {}", balancesForA001AfterTransfer);

        final List<Balance> balancesForA003AfterTransfer = banking.getBalancesForAccount("A003");
        log.info("Balances for Account A003 after Transfer {}", balancesForA003AfterTransfer);
    }

    /**
     * <p>
     * In this case, we demonstrate how the OCC can get transparently handled
     * if you use the convenience method (.execute) of the qldbSession.
     * If you want  more control on your qldb transactions and manage the OCC
     * yourself, check the examples in "ManualQLDBTransactions" class
     * </p>
     *
     * <p>
     * Example used in this method:
     * There are three transactions we want to do, in parallel.
     * 1) Transfer $500 from Account A001 to Account A003
     * 2) Transfer $400 from Account A001 to Account A004
     * 3) Transfer $300 from Account A003 to Account A004
     * </p>
     *
     * <p>
     * Let's assume that all the accounts have enough balances in order to do
     * the transactions (no case of overdraw) We first log the balances of
     * Account A001, A003 and A004. This logging is just for debugging and not
     * part of the banking activity.
     *
     * At the end of these transactions:
     * 1) The USD balance in account A001 should be $900 less than what we
     * logged initially
     *
     * 2) The USD balance in account A003 should be $200 more than what we
     * logged initially(since it gets $500 from A001 and gives $300 to A004)
     *
     * 3) The USD balance in account A004 should be $700 more than what we
     * logged initially
     * </p>
     */
    public void runParallelTransfers() throws InterruptedException {
        //check the Balances for all the accounts before  beginning the transfers
        final List<Balance> balancesForA001BeforeTransfer = banking.getBalancesForAccount("A001");
        log.info("Balances for Account A001 before Transfer {}", balancesForA001BeforeTransfer);

        final List<Balance> balancesForA003BeforeTransfer = banking.getBalancesForAccount("A003");
        log.info("Balances for Account A003 before Transfer {}", balancesForA003BeforeTransfer);

        final List<Balance> balancesForA004BeforeTransfer = banking.getBalancesForAccount("A004");
        log.info("Balances for Account A004 before Transfer {}", balancesForA004BeforeTransfer);


        final Callable<Void> transfer1 = () -> {
            log.debug("Starting Transfer 1");
            banking.transfer(
                    TransferRequest.builder()
                            .senderAccountId("A001")
                            .receiverAccountId("A003")
                            .currency("USD")
                            .amount(500)
                            .notes("A001 pays A003")
                            .build()
            );
            return null;
        };

        final Callable<Void> transfer2 = () -> {
            log.debug("Starting Transfer 2");
            banking.transfer(
                    TransferRequest.builder()
                            .senderAccountId("A001")
                            .receiverAccountId("A004")
                            .currency("USD")
                            .amount(400)
                            .notes("A001 pays A004")
                            .build()
            );
            return null;
        };

        final Callable<Void> transfer3 = () -> {
            log.debug("Starting Transfer 3");
            banking.transfer(
                    TransferRequest.builder()
                            .senderAccountId("A003")
                            .receiverAccountId("A004")
                            .currency("USD")
                            .amount(300)
                            .notes("A003 pays A004")
                            .build()
            );
            return null;
        };

        final List<Callable<Void>> tasksToExecute = Arrays.asList(transfer1, transfer2, transfer3);

        final ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.invokeAll(tasksToExecute);
        executorService.shutdown();

        //check the Balances for all the accounts after the transfers
        final List<Balance> balancesForA001AfterTransfer = banking.getBalancesForAccount("A001");
        log.info("Balances for Account A001 after Transfer {}", balancesForA001AfterTransfer);

        final List<Balance> balancesForA003AfterTransfer = banking.getBalancesForAccount("A003");
        log.info("Balances for Account A003 after Transfer {}", balancesForA003AfterTransfer);

        final List<Balance> balancesForA004AfterTransfer = banking.getBalancesForAccount("A004");
        log.info("Balances for Account A004 after Transfer {}", balancesForA004AfterTransfer);
    }

    public static void main(String... args) {
        final BankingComponent bankingComponent = DaggerBankingComponent.builder().build();
        final TransferMoney transferMoney = bankingComponent.providesTransferMoney();

        if (args.length == 0 || args[0].equals("singleTransfer")) {
            transferMoney.runSingleTransfer();
        } else if (args[0].equals("parallelTransfers")) {
            try {
                transferMoney.runParallelTransfers();
            } catch (final InterruptedException e) {
                log.info("Interrupted while running the parallel Transfers", e);
            }
        } else {
            log.error("Unknown arguments {}",(Object[]) args);
        }
    }
}
