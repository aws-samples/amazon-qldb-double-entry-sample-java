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

package software.amazon.qldb.doubleentry.helpers;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.qldb.Executor;
import software.amazon.qldb.ExecutorNoReturn;
import software.amazon.qldb.PooledQldbDriver;
import software.amazon.qldb.QldbSession;
import software.amazon.qldb.RetryIndicator;

/**
 * <p>
 * This is a single place to manage all the interactions with the QLDB Driver.
 * The class takes care of injecting the correct driver, getting the session
 * from the driver, issuing the execute method of the session and handling the
 * exceptions coming due to bad sessions.
 * </p>
 *
 * <p>
 * All the classes in the application which require to interact with QLDB
 * inject the TransactionHandler as a dependency and then just use
 * "executeTransaction" method. This gives two advantages:
 * </p>
 * <ul>
 *     <li> All the other classes are freed from the responsibility of managing
 *     the driver and sessions </li>
 *
 *     <li> It becomes easier to debug and maintain the interactions with QLDB
 *     driver, since it resides only in TransactionHandler</li>
 * </ul>
 *
 * Note: This is just one of the patterns that can be used in production.
 */

@Slf4j
public class TransactionsHandler {

    private PooledQldbDriver pooledQldbDriver;

    public TransactionsHandler(@NonNull final PooledQldbDriver pooledQldbDriver) {
        this.pooledQldbDriver = pooledQldbDriver;
    }

    /**
     * Using this method, you do not need to return anything from the transaction.
     * @param executorNoReturn The anonymous function that has to be executed
     *                         as a part of the transaction. This function does
     *                         not return any value
     * @param retryIndicator  The function to be executed on retry
     */
    public void executeTransactionWithNoReturn(final ExecutorNoReturn executorNoReturn, RetryIndicator retryIndicator) {
        try (final QldbSession qldbSession = pooledQldbDriver.getSession()) {
            qldbSession.execute(executorNoReturn, retryIndicator);
        }
    }


    /**
     * Using this method, you can return results from the transaction
     * @param executor The anonymous function that has to be executed
     *                         as a part of the transaction. This function does
     *                         not return any value
     * @param retryIndicator  The function to be executed on retry
     */
    public <T> T executeTransaction(Executor<T> executor, RetryIndicator retryIndicator) {
        try (final QldbSession qldbSession = pooledQldbDriver.getSession()) {
            return qldbSession.execute(executor, retryIndicator);
        }
    }
}
