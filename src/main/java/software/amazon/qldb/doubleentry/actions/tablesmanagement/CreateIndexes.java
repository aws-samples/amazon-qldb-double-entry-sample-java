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
 * Create indexes on the tables
 */
@Slf4j
public class CreateIndexes {

    private TransactionsHandler transactionsHandler;

    public CreateIndexes(@NonNull final TransactionsHandler transactionsHandler) {
        this.transactionsHandler = transactionsHandler;
    }

    private int createIndex(final TransactionExecutor txn,
                              final String tableName,
                              final String indexAttribute) {
        log.info("Creating an index on {}...", indexAttribute);
        final String createIndex = String.format("CREATE INDEX ON %s (%s)",
                tableName, indexAttribute);
        final Result result = txn.execute(createIndex);
        return IonHelper.toIonValues(result).size();
    }

    public void createAllIndexes() {
        transactionsHandler.executeTransactionWithNoReturn(txn-> {
            createIndex(txn, Constants.BUSINESSES_TABLE_NAME, Constants.BUSINESS_ID_INDEX_NAME);
            createIndex(txn, Constants.ACCOUNTS_TABLE_NAME, Constants.ACCOUNT_ID_INDEX_NAME);
            createIndex(txn, Constants.ACCOUNTS_TABLE_NAME, Constants.BUSINESS_ID_INDEX_NAME);
        }, (retryAttempt) -> log.info("Retrying due to OCC conflict..."));
    }

}
