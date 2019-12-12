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

package software.amazon.qldb.doubleentry.dagger.modules;


import com.amazonaws.services.qldb.AmazonQLDB;
import dagger.Module;
import dagger.Provides;
import software.amazon.qldb.doubleentry.actions.ledgermanagement.CreateLedger;
import software.amazon.qldb.doubleentry.actions.ledgermanagement.DescribeLedger;
import software.amazon.qldb.doubleentry.actions.ledgermanagement.ListLedgers;
import software.amazon.qldb.doubleentry.actions.tablesmanagement.CreateIndexes;
import software.amazon.qldb.doubleentry.actions.tablesmanagement.CreateTables;
import software.amazon.qldb.doubleentry.helpers.TransactionsHandler;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class SetupModule {

    @Provides
    @Singleton
    @Named("createLedger")
    CreateLedger providesCreateLedger(@Named("qldbClient") AmazonQLDB client) {
        return new CreateLedger(client);
    }

    @Provides
    @Singleton
    @Named("describeLedger")
    DescribeLedger providesDescribeLedger(@Named("qldbClient") AmazonQLDB client) {
        return new DescribeLedger(client);
    }

    @Provides
    @Singleton
    @Named("listLedgers")
    ListLedgers providesListLedgers(@Named("qldbClient") AmazonQLDB client) {
        return new ListLedgers(client);
    }

    @Provides
    @Singleton
    @Named("createTables")
    CreateTables providesCreateTables(@Named("transactionHandler") final TransactionsHandler transactionsHandler) {
        return new CreateTables(transactionsHandler);
    }

    @Provides
    @Singleton
    @Named("createIndexes")
    CreateIndexes providesCreateIndexes(@Named("transactionHandler") final TransactionsHandler transactionsHandler) {
        return new CreateIndexes(transactionsHandler);
    }
}
