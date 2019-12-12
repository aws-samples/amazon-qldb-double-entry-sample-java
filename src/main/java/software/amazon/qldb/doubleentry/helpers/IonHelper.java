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

import com.amazon.ion.Decimal;
import com.amazon.ion.IonStruct;
import com.amazon.ion.IonValue;
import com.fasterxml.jackson.dataformat.ion.IonObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.qldb.Result;
import software.amazon.qldb.doubleentry.models.qldb.DmlResultDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class IonHelper {

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private IonObjectMapper ionObjectMapper;

    public IonHelper(@NonNull final IonObjectMapper ionObjectMapper) {
        this.ionObjectMapper = ionObjectMapper;
    }

    /**
     * Wrapper method on IonObjectMapper's Method that can be used to
     * map any Java value to an IonValue.
     */
    public IonValue toIonValue(final Object value) {
        try {
            return ionObjectMapper.writeValueAsIonValue(value);
        } catch (final IOException e) {
            log.error("Error converting value {} to IonValue", value, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenience method for converting Ion value into given value type.
     */
    public <T> T readIonValue(final IonValue value, final Class<T> valueType) {
        try {
            return ionObjectMapper.readValue(value, valueType);
        } catch (final IOException e) {
            log.error("Error converting IonValue {} to type {}", value, valueType, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenience method for converting the QLDB Session Result into IonValue
     */
    public static List<IonValue> toIonValues(Result result) {
        final List<IonValue> valueList = new ArrayList<>();
        result.iterator().forEachRemaining(valueList::add);
        return valueList;
    }

    public Decimal convertToDecimal(final double num) {
        return Decimal.valueOf(num);
    }

    public LocalDate convertToLocalDate(String date) {
        return LocalDate.parse(date, DATE_TIME_FORMAT);
    }

    /**
     * Convenience method for extracting the document Ids from a Result object
     */
    public List<String> getDocumentIdsFromDmlResult(final Result result) {
        final List<String> strings = new ArrayList<>();
        result.iterator().forEachRemaining(row -> strings.add(getDocumentIdFromDmlResultDocument(row)));
        return strings;
    }

    /**
     * Given a single IonValue representing the QLDB document, returns the documentId
     */
    public String getDocumentIdFromDmlResultDocument(final IonValue dmlResultDocument) {
        try {
            final DmlResultDocument result = ionObjectMapper.readValue(dmlResultDocument, DmlResultDocument.class);
            return result.getDocumentId();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    /**
     * Convert the Result Object into IonStructs. IonStruct are useful for navigating the
     * document and extracting fields of interest
     */
    public List<IonStruct> toIonStructs(final Result result) {
        final List<IonStruct> documentList = new ArrayList<>();
        result.iterator().forEachRemaining(row -> documentList.add((IonStruct) row));
        return documentList;
    }

}
