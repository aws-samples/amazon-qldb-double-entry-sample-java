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

package software.amazon.qldb.doubleentry.actions.ledgermanagement;

import com.amazonaws.services.qldb.AmazonQLDB;
import com.amazonaws.services.qldb.model.LedgerSummary;
import com.amazonaws.services.qldb.model.ListLedgersRequest;
import com.amazonaws.services.qldb.model.ListLedgersResult;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * List all QLDB ledgers in a given account.
 *
 * This code expects that you have AWS credentials setup per:
 * http://docs.aws.amazon.com/java-sdk/latest/developer-guide/setup-credentials.html
 */
@Slf4j
public class ListLedgers {

    private AmazonQLDB client;

    public ListLedgers(@NonNull final AmazonQLDB client) {
        this.client = client;
    }
    /**
     * List all ledgers.
     */
    public  List<LedgerSummary> list() {
        List<LedgerSummary> ledgerSummaries = new ArrayList<>();
        String nextToken = null;
        do {
            ListLedgersRequest request = new ListLedgersRequest().withNextToken(nextToken);
            ListLedgersResult result = client.listLedgers(request);
            ledgerSummaries.addAll(result.getLedgers());
            nextToken = result.getNextToken();
        } while (nextToken != null);
        return ledgerSummaries;
    }
}
