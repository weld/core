/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.enablement;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.weld.util.Preconditions;

/**
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
class Item implements Comparable<Item> {

    static final int ITEM_PRIORITY_SCALE_POWER = 10;

    private final Class<?> javaClass;

    private final int originalPriority;

    private final AtomicInteger priority;

    Item(Class<?> javaClass, int priority) {
        this(javaClass, priority, priority);
    }

    Item(Class<?> javaClass, int originalPriority, int priority) {
        Preconditions.checkArgumentNotNull(javaClass, "javaClass");
        Preconditions.checkArgumentNotNull(priority, "priority");
        Preconditions.checkArgumentNotNull(originalPriority, "originalPriority");
        this.javaClass = javaClass;
        this.priority = new AtomicInteger(priority);
        this.originalPriority = originalPriority;
    }

    void scalePriority() {
        priority.getAndUpdate((p) -> p * ITEM_PRIORITY_SCALE_POWER);
    }

    Class<?> getJavaClass() {
        return javaClass;
    }

    int getPriority() {
        return priority.get();
    }

    int getOriginalPriority() {
        return originalPriority;
    }

    int getNumberOfScaling() {
        int current = priority.get();
        if (current == originalPriority) {
            return 0;
        }
        int scaling = 0;
        do {
            current = current / ITEM_PRIORITY_SCALE_POWER;
            scaling++;
        } while (current != originalPriority);
        return scaling;
    }

    @Override
    public int compareTo(Item o) {
        int p1 = priority.get();
        int p2 = o.priority.get();
        if (p1 == p2) {
            /*
             * The spec does not specify what happens if two records have the same priority. Instead of giving random results,
             * we compare the records based on
             * their class name lexicographically.
             */
            return javaClass.getName().compareTo(o.javaClass.getName());
        } else if (p1 < p2) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public int hashCode() {
        return javaClass.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Item) {
            Item that = (Item) obj;
            return Objects.equals(javaClass, that.javaClass);
        }
        return false;
    }

    @Override
    public String toString() {
        return "[Class=" + javaClass + ", priority=" + priority + "]";
    }
}