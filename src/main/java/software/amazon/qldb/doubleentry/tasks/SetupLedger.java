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

import com.amazonaws.services.qldb.model.DescribeLedgerResult;
import com.amazonaws.services.qldb.model.LedgerState;
import com.amazonaws.services.qldb.model.LedgerSummary;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.qldb.doubleentry.Constants;
import software.amazon.qldb.doubleentry.actions.ledgermanagement.CreateLedger;
import software.amazon.qldb.doubleentry.actions.ledgermanagement.DescribeLedger;
import software.amazon.qldb.doubleentry.actions.ledgermanagement.ListLedgers;
import software.amazon.qldb.doubleentry.dagger.components.DaggerSetupComponent;
import software.amazon.qldb.doubleentry.dagger.components.SetupComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * The purpose of this class is to show how a ledger can be setup using the QLDB java driver.
 * We use three actions  to do that:
 * <ol>
 *     <li>CreateLedger</li>
 *     <li>DescribeLedger</li>
 *     <li>ListLedgers</li>
 * </ol>
 * We first issue a CreateLedger query to QLDB, which starts the ledger creation process. At this point of time,
 * the status of the ledger will be  "CREATING" as QLDB takes short amount of time to create the ledger.
 *
 * In Order to confirm if the ledger creation is complete, we poll QLDB via DescribeLedger Query at a fixed interval
 * and check if the Status has changed to Active. Once the status has changed to ACTIVE, we are sure that the ledger
 * creation has succeeded.
 *
 * Finally, We use the ListLedgers query to log all the ledgers we have created up until now.
 */
@Slf4j
public class SetupLedger {

    public static final Long LEDGER_CREATION_POLL_PERIOD_MS = 10_000L;

    private CreateLedger createLedger;

    private DescribeLedger describeLedger;

    private ListLedgers listLedgers;

    public SetupLedger(@NonNull final CreateLedger createLedger,
                       @NonNull final DescribeLedger describeLedger,
                       @NonNull final ListLedgers listLedgers) {
        this.createLedger = createLedger;
        this.describeLedger = describeLedger;
        this.listLedgers = listLedgers;
    }

    private void createLedgerAndWaitForActivation(final String ledgerName) {
        try {
            log.info("Creating the ledger with name {}", ledgerName);
            createLedger.create(ledgerName);
            log.info("We wait for ledger to become active");
            waitForActive(ledgerName);
        } catch (final Exception ex) {
            log.error("Oops, Ledger Creation (ledger name =  {}) failed", ledgerName, ex);
        }
    }

    /**
     * Wait for a newly created ledger to become active.
     *
     */
    private void waitForActive(final String ledgerName) {
        try {
            while (true) {
                final DescribeLedgerResult result = describeLedger.describe(ledgerName);
                if (result.getState().equals(LedgerState.ACTIVE.name())) {
                    log.info("Success. Ledger is active and ready to use.{}", result);
                    return;
                }
                log.info("The ledger is still creating. Please wait.... Current state: {}", result);
                Thread.sleep(LEDGER_CREATION_POLL_PERIOD_MS);
            }
        } catch (final InterruptedException ex) {
            log.warn("Interrupted while waiting for Ledger to be active. Check AWS console to see the " +
                    "activation status of ledger {}", ledgerName);
        }
    }

    private List<LedgerSummary> listLedgers(){
        List<LedgerSummary> ledgerSummaryList = new ArrayList<>();
        try {
            ledgerSummaryList = listLedgers.list();
            log.info("All ledgers present in the account are {}", ledgerSummaryList);
        } catch (final Exception ex) {
            log.error("Something Went wrong while Listing the ledgers");
        }
        return ledgerSummaryList;
    }

    private void run() {
        createLedgerAndWaitForActivation(Constants.LEDGER_NAME);
        log.info("We list all the ledgers we have created till now");
        listLedgers();
    }

    public static void main(String[] args) {
        final SetupComponent setupComponent = DaggerSetupComponent.builder().build();
        setupComponent.providesSetupLedger().run();
    }

}
