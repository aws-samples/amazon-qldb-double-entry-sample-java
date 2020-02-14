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

package software.amazon.qldb.doubleentry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constant values used throughout this tutorial.
 */
public final class Constants {

    public static final int RETRY_LIMIT = 4;
    public static final String LEDGER_NAME = "double-entry-ledger";

    public static final String BUSINESSES_TABLE_NAME = "Businesses";
    public static final String ACCOUNTS_TABLE_NAME = "Accounts";
    public static final String TRANSACTIONS_TABLE_NAME = "Transactions";
    public static final String BUSINESS_ID_INDEX_NAME = "BusinessId";
    public static final String ACCOUNT_ID_INDEX_NAME = "AccountId";

    public static final List<String> SUPPORTED_CURRENCIES = Collections.unmodifiableList(
            Arrays.asList("USD", "JPY", "INR", "SGD"));

    private Constants() { }
}
