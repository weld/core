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
package org.jboss.weld.tests.event.observer.transactional;

import java.util.ArrayList;
import java.util.List;

public class Actions {

    private static List<String> actions = new ArrayList<String>();

    public static List<String> getActions() {
        return actions;
    }

    public static void clear() {
        actions.clear();
    }

    public static boolean add(Object o) {
        return actions.add(o.toString());
    }

    public static boolean isSequence(Object... seq) {
        int i = 0;
        return objectsToStrings(seq).equals(actions);
    }

    // true iff obj exists and all otherObjects exist and indexOf(obj) < indexOf(x) for each x from otherObjects
    public static boolean precedes(Object obj, Object... otherObjects) {
        boolean precedes = true;
        int i = 0;
        if (precedes = (Actions.contains(obj) && Actions.contains(otherObjects))) {
            while (i < otherObjects.length
                    && (precedes = precedes && actions.indexOf(obj.toString()) < actions.indexOf(otherObjects[i++].toString())))
                ;
        }
        return precedes;
    }

    public static boolean startsWith(Object... objects) {
        return actions.subList(0, objects.length).equals(objectsToStrings(objects));
    }

    public static boolean endsWith(Object... objects) {
        return actions.subList(actions.size() - objects.length, actions.size()).equals(objectsToStrings(objects));
    }

    public static boolean contains(Object... objects) {
        return actions.containsAll(objectsToStrings(objects));
    }

    private static List<String> objectsToStrings(final Object... objects) {
        List<String> result = new ArrayList<String>();
        for (Object obj : objects) {
            result.add(obj.toString());
        }
        return result;
    }
}
