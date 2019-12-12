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
import com.amazonaws.services.qldb.AmazonQLDBClientBuilder;
import com.amazonaws.services.qldbsession.AmazonQLDBSessionClientBuilder;
import dagger.Module;
import dagger.Provides;
import software.amazon.qldb.PooledQldbDriver;
import software.amazon.qldb.doubleentry.Constants;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class DriverClientModule {

    @Provides
    @Singleton
    @Named("driverForDoubleEntryLedger")
    public PooledQldbDriver providesQLDBDriver(
            @Named("qldbSessionClientBuilder") final AmazonQLDBSessionClientBuilder builder) {
        return PooledQldbDriver.builder()
                .withLedger(Constants.LEDGER_NAME)
                .withRetryLimit(Constants.RETRY_LIMIT)
                .withSessionClientBuilder(builder)
                .build();
    }

    @Provides
    @Singleton
    @Named("qldbSessionClientBuilder")
    public AmazonQLDBSessionClientBuilder providesSessionClientBuilder() {
        return AmazonQLDBSessionClientBuilder.standard();
    }

    @Provides
    @Singleton
    @Named("qldbClient")
    public AmazonQLDB providesQLDBClient() {
        return AmazonQLDBClientBuilder.standard().build();
    }


}
