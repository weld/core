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
package org.jboss.weld.tests.cditck11.event.observer.transactional.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple data holder for sequence of actions identified with {@link String}.
 * 
 * @author Martin Kouba
 */
public final class ActionSequence {

    /**
     * Name of sequence
     */
    private String name;

    /**
     * Data - list of actions
     */
    private List<String> data = Collections.synchronizedList(new ArrayList<String>());

    public ActionSequence() {
        super();
        this.name = DEFAULT_SEQUENCE;
    }

    public ActionSequence(String name) {
        super();
        this.name = name;
    }

    /**
     * @param actionId
     * @return data holder
     */
    public ActionSequence add(String actionId) {
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
     * 
     * @param actions
     * @return <code>true</code> if sequence data contain all of the specified actions, <code>false</code> otherwise
     */
    public boolean containsAll(String... actions) {
        return getData().containsAll(Arrays.asList(actions));
    }

    /**
     * 
     * @param actions
     * @return <code>true</code> if sequence data begins with the specified actions, <code>false</code> otherwise
     */
    public boolean beginsWith(String... actions) {

        List<String> sequenceData = getData();
        List<String> matchData = Arrays.asList(actions);

        if (sequenceData.size() < matchData.size())
            return false;

        return sequenceData.subList(0, matchData.size()).equals(matchData);
    }

    /**
     * 
     * @param actions
     * @return <code>true</code> if sequence data ends with the specified actions, <code>false</code> otherwise
     */
    public boolean endsWith(String... actions) {

        List<String> sequenceData = getData();
        List<String> matchData = Arrays.asList(actions);

        if (sequenceData.size() < matchData.size())
            return false;

        return sequenceData.subList(sequenceData.size() - matchData.size(), sequenceData.size()).equals(matchData);
    }

    @Override
    public String toString() {
        return String.format("ActionSequence [name=%s, data=%s]", name, getData());
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
     * @return <code>true</code> if a new sequence was added, <code>false</code> otherwise
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
     * @return <code>true</code> if a new sequence was added, <code>false</code> otherwise
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
     * @return specified sequence or <code>null</code> if no such sequence exists
     */
    public static ActionSequence getSequence(String sequenceName) {
        synchronized (sequences) {
            return sequences.get(sequenceName);
        }
    }

    /**
     * @return data of default sequence or <code>null</code> if no such sequence exists
     */
    public static List<String> getSequenceData() {
        return getSequenceData(DEFAULT_SEQUENCE);
    }

    /**
     * @param sequenceName
     * @return data of specified sequence or <code>null</code> if no such sequence exists
     */
    public static List<String> getSequenceData(String sequenceName) {
        synchronized (sequences) {
            return sequences.containsKey(sequenceName) ? sequences.get(sequenceName).getData() : null;
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
            return sequences.containsKey(sequenceName) ? sequences.get(sequenceName).getData().size() : 0;
        }
    }

}
