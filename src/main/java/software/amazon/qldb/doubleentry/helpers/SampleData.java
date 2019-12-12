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
import software.amazon.qldb.doubleentry.models.Account;
import software.amazon.qldb.doubleentry.models.Balance;
import software.amazon.qldb.doubleentry.models.Business;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sample data to be loaded into the tables in ledger
 */
@Slf4j
public class SampleData {

    private IonHelper ionHelper;

    public SampleData(@NonNull final IonHelper ionHelper) {
        this.ionHelper = ionHelper;
    }

    public List<Business> getBusinesses() {
        final List<Business> businesses = Collections.unmodifiableList(
                Arrays.asList(
                        Business.builder()
                                .businessId("B001")
                                .name("ABC")
                                .registeredAddress("Seattle, WA")
                                .registrationDate(ionHelper.convertToLocalDate("2008-01-10"))
                                .build(),
                        Business.builder()
                                .businessId("B002")
                                .name("XYZ")
                                .registeredAddress("Seattle, WA")
                                .registrationDate(ionHelper.convertToLocalDate("2009-11-20"))
                                .build()
                ));
        return businesses;
    }

    public List<Account> getAccounts() {
        final List<Account> accounts = Collections.unmodifiableList(Arrays.asList(
                Account.builder()
                        .accountId("A001")
                        .businessId("B001")
                        .accountType("Payable")
                        .balances(Arrays.asList(
                                Balance.builder()
                                        .currency("USD")
                                        .currencyBalance(ionHelper.convertToDecimal(230000))
                                        .build(),
                                Balance.builder()
                                        .currency("INR")
                                        .currencyBalance(ionHelper.convertToDecimal(344500))
                                        .build(),
                                Balance.builder()
                                        .currency("JPY")
                                        .currencyBalance(ionHelper.convertToDecimal(450))
                                        .build(),
                                Balance.builder()
                                        .currency("SGD")
                                        .currencyBalance(ionHelper.convertToDecimal(857075))
                                        .build()))
                        .build(),
                Account.builder()
                        .accountId("A002")
                        .businessId("B001")
                        .accountType("Available")
                        .balances(Arrays.asList(
                                Balance.builder()
                                        .currency("USD")
                                        .currencyBalance(ionHelper.convertToDecimal(75656))
                                        .build(),
                                Balance.builder()
                                        .currency("INR")
                                        .currencyBalance(ionHelper.convertToDecimal(565897))
                                        .build(),
                                Balance.builder()
                                        .currency("JPY")
                                        .currencyBalance(ionHelper.convertToDecimal(575781))
                                        .build(),
                                Balance.builder()
                                        .currency("SGD")
                                        .currencyBalance(ionHelper.convertToDecimal(53426))
                                        .build()))
                        .build(),
                Account.builder()
                        .accountId("A003")
                        .businessId("B002")
                        .accountType("Pending")
                        .balances(Arrays.asList(
                                Balance.builder()
                                        .currency("USD")
                                        .currencyBalance(ionHelper.convertToDecimal(65799))
                                        .build(),
                                Balance.builder()
                                        .currency("INR")
                                        .currencyBalance(ionHelper.convertToDecimal(787656))
                                        .build()))
                        .build(),
                Account.builder()
                        .accountId("A004")
                        .businessId("B002")
                        .accountType("Available")
                        .balances(Arrays.asList(
                                Balance.builder()
                                        .currency("USD")
                                        .currencyBalance(ionHelper.convertToDecimal(88000))
                                        .build(),
                                Balance.builder()
                                        .currency("INR")
                                        .currencyBalance(ionHelper.convertToDecimal(78600))
                                        .build()))
                        .build()
        ));
        return accounts;
    }


}
