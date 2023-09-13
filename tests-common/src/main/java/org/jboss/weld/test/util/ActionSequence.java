/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Simple data holder for sequence of actions identified with {@link String}.
 * <p>
 * Always call {@link #reset()} before your test code to remove previous
 * sequences stored in static map!
 *
 * @author Martin Kouba
 */
public final class ActionSequence {

    private static final Pattern VALID_NAME_PATTERN = Pattern
            .compile("[a-zA-Z0-9_.$\\-]+");

    private static final TransformationUtils.Function<Class<?>, String> GET_SIMPLE_NAME = new TransformationUtils.Function<Class<?>, String>() {
        @Override
        public String apply(Class<?> input) {
            return input.getSimpleName();
        }
    };

    private String name;

    /**
     * Data - list of actions
     */
    private List<String> data = Collections
            .synchronizedList(new ArrayList<String>());

    public ActionSequence() {
        super();
        this.name = DEFAULT_SEQUENCE;
    }

    /**
     * @param name
     */
    public ActionSequence(String name) {
        super();
        checkStringValue(name);
        this.name = name;
    }

    /**
     * @param actionId
     * @return data holder
     */
    public ActionSequence add(String actionId) {
        checkStringValue(actionId);
        this.data.add(actionId);
        return this;
    }

    /**
     * @return read-only copy of sequence data
     */
    public List<String> getData() {
        return Collections.unmodifiableList(new ArrayList<String>(this.data));
    }

    /**
     * @return name of sequence
     */
    public String getName() {
        return name;
    }

    /**
     * @param actions
     * @return <code>true</code> if sequence data contain all of the specified
     *         actions, <code>false</code> otherwise
     */
    public boolean containsAll(String... actions) {
        return getData().containsAll(Arrays.asList(actions));
    }

    /**
     * @param actions
     * @return <code>true</code> if sequence data begins with the specified
     *         actions, <code>false</code> otherwise
     */
    public boolean beginsWith(String... actions) {

        List<String> sequenceData = getData();
        List<String> matchData = Arrays.asList(actions);

        if (sequenceData.size() < matchData.size()) {
            return false;
        }

        return sequenceData.subList(0, matchData.size()).equals(matchData);
    }

    /**
     * @param actions
     * @return <code>true</code> if sequence data ends with the specified
     *         actions, <code>false</code> otherwise
     */
    public boolean endsWith(String... actions) {

        List<String> sequenceData = getData();
        List<String> matchData = Arrays.asList(actions);

        if (sequenceData.size() < matchData.size()) {
            return false;
        }

        return sequenceData.subList(sequenceData.size() - matchData.size(),
                sequenceData.size()).equals(matchData);
    }

    @Override
    public String toString() {
        return String.format("ActionSequence [name=%s, data=%s]", name,
                getData());
    }

    /**
     * @return data in simple CSV format
     */
    public String dataToCsv() {
        if (data.isEmpty()) {
            return "";
        }
        StringBuilder csv = new StringBuilder();
        for (Iterator<String> iterator = data.iterator(); iterator.hasNext();) {
            String actionId = iterator.next();
            csv.append(actionId);
            if (iterator.hasNext()) {
                csv.append(",");
            }
        }
        return csv.toString();
    }

    /**
     * Assert that strings stored in this sequence equal (in order!) to the
     * {@code expected} strings.
     *
     * @param expected
     */
    public void assertDataEquals(List<String> expected) {
        assertEquals(String.format("%s and expected sequence differ in size.",
                toString()), expected.size(), data.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(String.format(
                    "%s and expected sequence differ on the index %d.",
                    toString(), i), data.get(i), expected.get(i));
        }
    }

    /**
     * Assert that strings stored in this sequence equal (in order!) to the
     * {@code expected} strings.
     *
     * @param expected
     */
    public void assertDataEquals(String... expected) {
        assertDataEquals(Arrays.asList(expected));
    }

    /**
     * Assert that strings stored in this sequence equal (in order!) to the
     * simple class names of the {@code expected} classes.
     *
     * @param expected
     */
    public void assertDataEquals(Class<?>... expected) {
        assertDataEquals(TransformationUtils.transform(GET_SIMPLE_NAME,
                expected));
    }

    /**
     * Assert that this sequence contains all of the {@code expected} strings.
     * Note that this only verifies that the {@code expected} strings are a
     * SUBSET of the actual strings stored in this sequence.
     *
     * @param expected
     */
    public void assertDataContainsAll(Collection<String> expected) {
        for (String s : expected) {
            assertTrue(String.format("%s does not contain %s", toString(), s),
                    data.contains(s));
        }
    }

    /**
     * Assert that this sequence contains all of the {@code expected} strings.
     * Note that this only verifies that the {@code expected} strings are a
     * SUBSET of the actual strings stored in this sequence.
     *
     * @param expected
     */
    public void assertDataContainsAll(String... expected) {
        assertDataContainsAll(Arrays.asList(expected));
    }

    /**
     * Assert that this sequence contains simple class names of all of the
     * {@code expected} classes. Note that this only verifies that the
     * {@code expected} classes are a SUBSET of the actual classes stored in
     * this sequence.
     *
     * @param expected
     */
    public void assertDataContainsAll(Class<?>... expected) {
        assertDataContainsAll(TransformationUtils.transform(GET_SIMPLE_NAME,
                expected));
    }

    // Static members

    private static final String DEFAULT_SEQUENCE = "default";

    /**
     * Static sequence map
     */
    private static Map<String, ActionSequence> sequences = new HashMap<String, ActionSequence>();

    /**
     * Remove all sequences.
     */
    public static void reset() {
        synchronized (sequences) {
            sequences.clear();
        }
    }

    /**
     * Add actionId to specified sequence. Add new sequence if needed.
     *
     * @param sequence
     * @param actionId
     * @return <code>true</code> if a new sequence was added, <code>false</code>
     *         otherwise
     */
    public static boolean addAction(String sequenceName, String actionId) {

        boolean newSequenceAdded = false;

        synchronized (sequences) {

            if (!sequences.containsKey(sequenceName)) {
                sequences.put(sequenceName, new ActionSequence(sequenceName));
                newSequenceAdded = true;
            }
            sequences.get(sequenceName).add(actionId);
        }
        return newSequenceAdded;
    }

    /**
     * Add actionId to default sequence.
     *
     * @param actionId
     * @return <code>true</code> if a new sequence was added, <code>false</code>
     *         otherwise
     */
    public static boolean addAction(String actionId) {
        return addAction(DEFAULT_SEQUENCE, actionId);
    }

    /**
     * @return default sequence or <code>null</code> if no such sequence exists
     */
    public static ActionSequence getSequence() {
        return getSequence(DEFAULT_SEQUENCE);
    }

    /**
     * @param name
     * @return specified sequence or <code>null</code> if no such sequence
     *         exists
     */
    public static ActionSequence getSequence(String sequenceName) {
        synchronized (sequences) {
            return sequences.get(sequenceName);
        }
    }

    /**
     * @return data of default sequence or <code>null</code> if no such sequence
     *         exists
     */
    public static List<String> getSequenceData() {
        return getSequenceData(DEFAULT_SEQUENCE);
    }

    /**
     * @param sequenceName
     * @return data of specified sequence or <code>null</code> if no such
     *         sequence exists
     */
    public static List<String> getSequenceData(String sequenceName) {
        synchronized (sequences) {
            return sequences.containsKey(sequenceName) ? sequences.get(
                    sequenceName).getData() : null;
        }
    }

    /**
     * @return size of default sequence
     */
    public static int getSequenceSize() {
        return getSequenceSize(DEFAULT_SEQUENCE);
    }

    /**
     * @param sequence
     * @return size of specified sequence
     */
    public static int getSequenceSize(String sequenceName) {
        synchronized (sequences) {
            return sequences.containsKey(sequenceName) ? sequences
                    .get(sequenceName).getData().size() : 0;
        }
    }

    /**
     * @param csv
     * @return
     */
    public static ActionSequence buildFromCsvData(String csv) {

        if (csv == null) {
            throw new NullPointerException();
        }

        ActionSequence sequence = new ActionSequence();

        if (csv.length() != 0) {

            String[] data = csv.split(",");
            for (String actionId : data) {
                sequence.add(actionId);
            }
        }
        return sequence;
    }

    /**
     * Assert that strings stored in this sequence equal (in order!) to the
     * {@code expected} strings.
     *
     * @param expected
     * @throws IllegalStateException
     *         if there is no default sequence
     */
    public static void assertSequenceDataEquals(List<String> expected) {
        checkDefaultSequenceExists();
        getSequence().assertDataEquals(expected);
    }

    /**
     * Assert that strings stored in this sequence equal (in order!) to the
     * {@code expected} strings.
     *
     * @param expected
     * @throws IllegalStateException
     *         if there is no default sequence
     */
    public static void assertSequenceDataEquals(String... expected) {
        checkDefaultSequenceExists();
        getSequence().assertDataEquals(Arrays.asList(expected));
    }

    /**
     * Assert that strings stored in the default sequence equal (in order!) to
     * the simple class names of {@code expected}.
     *
     * @param expected
     * @throws IllegalStateException
     *         if there is no default sequence
     */
    public static void assertSequenceDataEquals(Class<?>... expected) {
        checkDefaultSequenceExists();
        getSequence().assertDataEquals(expected);
    }

    /**
     * Assert that this sequence contains all of the {@code expected} strings.
     * Note that this only verifies that the {@code expected} strings are a
     * SUBSET of the actual strings stored in this sequence.
     *
     * @param expected
     * @throws IllegalStateException
     *         if there is no default sequence
     */
    public static void assertSequenceDataContainsAll(Collection<String> expected) {
        checkDefaultSequenceExists();
        getSequence().assertDataContainsAll(expected);
    }

    /**
     * Assert that this sequence contains all of the {@code expected} strings.
     * Note that this only verifies that the {@code expected} strings are a
     * SUBSET of the actual strings stored in this sequence.
     *
     * @param expected
     * @throws IllegalStateException
     *         if there is no default sequence
     */
    public static void assertSequenceDataContainsAll(String... expected) {
        checkDefaultSequenceExists();
        getSequence().assertDataContainsAll(Arrays.asList(expected));
    }

    /**
     * Assert that the default sequence contains simple class names of all of
     * the {@code expected} classes. Note that this only verifies that the
     * {@code expected} classes are a SUBSET of the actual classes stored in
     * this sequence.
     *
     * @param expected
     * @throws IllegalStateException
     *         if there is no default sequence
     */
    public static void assertSequenceDataContainsAll(Class<?>... expected) {
        checkDefaultSequenceExists();
        getSequence().assertDataContainsAll(expected);
    }

    private static void checkStringValue(String value) {
        if (!VALID_NAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid name/id specified:"
                    + value);
        }
    }

    private static void checkDefaultSequenceExists() {
        if (getSequence() == null) {
            throw new IllegalStateException(
                    "There is no default sequence. You cannot assert anything about it.");
        }
    }

    public static final class TransformationUtils {

        private TransformationUtils() {

        }

        /**
         * @param <F>
         *        the input type of this function
         * @param <T>
         *        the output type of this function
         */
        public interface Function<F, T> {
            /**
             * Returns the result of applying this function to {@code input}.
             */
            T apply(F input);
        }

        /**
         * Returns a list that applies {@code function} to each element of
         * {@code fromCollection}.
         *
         * @throws IllegalArgumentException
         *         in case of a null argument
         */
        public static <F, T> List<T> transform(
                final Function<? super F, ? extends T> function,
                final Collection<F> fromCollection) {
            checkNotNull(fromCollection);
            checkNotNull(function);
            List<T> result = new ArrayList<T>(fromCollection.size());
            for (F element : fromCollection) {
                result.add(function.apply(element));
            }
            return result;
        }

        /**
         * Returns a list that applies {@code function} to each element of
         * {@code inputElements}.
         */
        @SafeVarargs
        public static <F, T> List<T> transform(
                final Function<? super F, ? extends T> function,
                final F... inputElements) {
            return transform(function, Arrays.asList(inputElements));
        }

        private static void checkNotNull(Object o) {
            if (o == null) {
                throw new IllegalArgumentException("Null argument.");
            }
        }
    }
}
