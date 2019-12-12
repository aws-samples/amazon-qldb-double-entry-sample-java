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

import com.amazon.ion.system.IonSystemBuilder;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.ion.IonObjectMapper;
import com.fasterxml.jackson.dataformat.ion.ionvalue.IonValueMapper;
import dagger.Module;
import dagger.Provides;
import software.amazon.qldb.PooledQldbDriver;
import software.amazon.qldb.doubleentry.actions.tablesmanagement.LoadSampleData;
import software.amazon.qldb.doubleentry.helpers.IonHelper;
import software.amazon.qldb.doubleentry.helpers.SampleData;
import software.amazon.qldb.doubleentry.helpers.TransactionsHandler;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class HelpersModule {

    @Provides
    @Named("transactionHandler")
    public TransactionsHandler providesTransactionHandler(
            @Named("driverForDoubleEntryLedger") final PooledQldbDriver pooledQldbDriver) {
        return new TransactionsHandler(pooledQldbDriver);
    }


    @Provides
    @Singleton
    @Named("ionObjectMapper")
    public IonObjectMapper providesIonObjectMapper() {
        final IonObjectMapper mapper = new IonValueMapper(IonSystemBuilder.standard().build());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        return mapper;
    }

    @Provides
    @Singleton
    @Named("ionHelper")
    public IonHelper providesIonHelper(@Named("ionObjectMapper") final IonObjectMapper ionObjectMapper) {
        final IonHelper ionHelper = new IonHelper(ionObjectMapper);
        return ionHelper;
    }

    @Provides
    @Singleton
    @Named("sampleData")
    public SampleData providesSampleData(@Named("ionHelper") final IonHelper ionHelper) {
        return new SampleData(ionHelper);
    }


    @Provides
    @Singleton
    @Named("loadSampleData")
    public LoadSampleData providesLoadSampleData(
            @Named("transactionHandler") final TransactionsHandler transactionsHandler,
            @Named("sampleData") final SampleData sampleData,
            @Named("ionHelper") final IonHelper ionHelper) {
        return new LoadSampleData(transactionsHandler, sampleData, ionHelper);
    }
}
