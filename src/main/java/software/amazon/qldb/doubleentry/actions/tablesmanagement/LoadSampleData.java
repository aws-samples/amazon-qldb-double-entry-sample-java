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

import com.amazon.ion.IonValue;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.qldb.Result;
import software.amazon.qldb.TransactionExecutor;
import software.amazon.qldb.doubleentry.Constants;
import software.amazon.qldb.doubleentry.helpers.IonHelper;
import software.amazon.qldb.doubleentry.helpers.SampleData;
import software.amazon.qldb.doubleentry.helpers.TransactionsHandler;

import java.util.Collections;
import java.util.List;

/**
 * Load sample data defined in {@link SampleData} into the tables
 */
@Slf4j
public class LoadSampleData {

    private TransactionsHandler transactionsHandler;

    private SampleData sampleData;


    private IonHelper ionHelper;

    public LoadSampleData(@NonNull final TransactionsHandler transactionsHandler,
                          @NonNull final SampleData sampleData,
                          @NonNull final IonHelper ionHelper) {
        this.transactionsHandler = transactionsHandler;
        this.sampleData = sampleData;
        this.ionHelper = ionHelper;
    }

    private List<String> insertDocuments(final TransactionExecutor txn,
                                         final String tableName,
                                         final List documents) {
        final String query = String.format("INSERT INTO %s ?", tableName);
        final IonValue ionDocuments = ionHelper.toIonValue(documents);

        final List<IonValue> parameters = Collections.singletonList(ionDocuments);
        final Result result = txn.execute(query, parameters);

        final List<String> insertedDocumentIds = ionHelper.getDocumentIdsFromDmlResult(result);
        log.info("List of inserted document ids {}", insertedDocumentIds);
        return insertedDocumentIds;
    }

    public void loadSampleDataForBusinesses() {
        transactionsHandler.executeTransactionWithNoReturn(txn -> {
            insertDocuments(txn, Constants.BUSINESSES_TABLE_NAME, sampleData.getBusinesses());
        }, (retryAttempt) -> log.info("Retrying due to OCC conflict..."));
    }

    public void loadSampleDataForAccounts() {
        transactionsHandler.executeTransactionWithNoReturn(txn -> {
            insertDocuments(txn, Constants.ACCOUNTS_TABLE_NAME, sampleData.getAccounts());
        }, (retryAttempt) -> log.info("Retrying due to OCC conflict..."));
    }
}
