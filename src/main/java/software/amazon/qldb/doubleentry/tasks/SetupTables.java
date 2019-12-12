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
import software.amazon.qldb.doubleentry.actions.tablesmanagement.CreateIndexes;
import software.amazon.qldb.doubleentry.actions.tablesmanagement.CreateTables;
import software.amazon.qldb.doubleentry.actions.tablesmanagement.LoadSampleData;
import software.amazon.qldb.doubleentry.dagger.components.DaggerSetupComponent;
import software.amazon.qldb.doubleentry.dagger.components.SetupComponent;

import javax.inject.Named;

/**
 * The purpose of this class is to show tables and indexes can be created using
 * QLDB java driver and how to load the sample data.
 * We use three actions do that:
 *
 * <ol>
 *     <li>CreateTables</li>
 *     <li>CreateIndexes</li>
 *     <li>LoadSampleData</li>
 * </ol>
 *
 * We start by  creating the tables using "CREATE TABLES" query of QLDB.
 * Once all the tables are created, we create the indexes on them using
 * "CREATE INDEX" query of QLDB.
 *
 * After the above two steps are completed, we load the sample data into the
 * tables
 */
@Slf4j
public class SetupTables {

    private CreateTables createTables;

    private CreateIndexes createIndexes;

    private LoadSampleData loadSampleData;

    public SetupTables(@Named("createTables") @NonNull final CreateTables createTables,
                       @Named("createIndexes") @NonNull final CreateIndexes createIndexes,
                       @Named("loadSampleData") @NonNull final LoadSampleData loadSampleData) {
        this.createTables = createTables;
        this.createIndexes = createIndexes;
        this.loadSampleData = loadSampleData;
    }

    public void run() {
        try {
            log.info("Creating tables");
            createTables.createAllTables();
            log.info("Creating Indexes on table");
            createIndexes.createAllIndexes();
            log.info("Load sample data for businesses");
            loadSampleData.loadSampleDataForBusinesses();
            log.info("Load sample data for Accounts");
            loadSampleData.loadSampleDataForAccounts();
        } catch (final Exception e) {
            log.error("Error while running setup tables", e);
        }
    }

    public static void main(String... args) {
        final SetupComponent setupComponent = DaggerSetupComponent.builder().build();
        setupComponent.providesSetupTables().run();
    }
}
