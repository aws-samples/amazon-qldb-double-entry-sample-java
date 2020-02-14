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

package software.amazon.qldb.doubleentry.actions.tablesmanagement;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.qldb.Result;
import software.amazon.qldb.TransactionExecutor;
import software.amazon.qldb.doubleentry.Constants;
import software.amazon.qldb.doubleentry.helpers.IonHelper;
import software.amazon.qldb.doubleentry.helpers.TransactionsHandler;

/**
 * Create tables in the given ledger
 */
@Slf4j
public class CreateTables {

    private TransactionsHandler transactionsHandler;

    public CreateTables(@NonNull final TransactionsHandler transactionsHandler) {
        this.transactionsHandler = transactionsHandler;
    }

    private int createTable(final TransactionExecutor txn, final String tableName) {
        log.info("Creating the '{}' table...", tableName);
        final String createTable = String.format("CREATE TABLE %s", tableName);
        final Result result = txn.execute(createTable);
        log.info("{} table created successfully.", tableName);
        return IonHelper.toIonValues(result).size();
    }

    public void createAllTables() {
        transactionsHandler.executeTransactionWithNoReturn(txn-> {
            createTable(txn, Constants.BUSINESSES_TABLE_NAME);
            createTable(txn, Constants.ACCOUNTS_TABLE_NAME);
            createTable(txn, Constants.TRANSACTIONS_TABLE_NAME);
        }, (retryAttempt) -> log.info("Retrying due to OCC conflict..."));
    }


}
