/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import org.jboss.weld.logging.BootstrapLogger;

/**
 *
 * @author Martin Kouba
 */
final class Trackers {

    private Trackers() {
    }

    private static final Tracker NOOP_INSTANCE = new NoopTracker() {
    };

    static Tracker create(String startOperation) {
        return create().start(startOperation);
    }

    static Tracker create() {
        return BootstrapLogger.TRACKER_LOG.isDebugEnabled() ? new LoggingTracker() : NOOP_INSTANCE;
    }

    private static class NoopTracker implements Tracker {

        @Override
        public Tracker start(String operation) {
            return this;
        }

        @Override
        public Tracker end() {
            return this;
        }

        @Override
        public void split(String info) {
        }

        @Override
        public void close() {
        }

    }

    private static class LoggingTracker implements Tracker {

        private final List<Operation> operations;

        LoggingTracker() {
            this.operations = new LinkedList<>();
        }

        @Override
        public Tracker start(String operation) {
            if (!operations.isEmpty()) {
                operation = operations.get(operations.size() - 1).name + " > " + operation;
            }
            operations.add(new Operation(operation));
            BootstrapLogger.TRACKER_LOG.debugf("START %s ", operation);
            return this;
        }

        @Override
        public void split(String info) {
            Operation operation = operations.get(operations.size() - 1);
            BootstrapLogger.TRACKER_LOG.debugf(" TIME %s:%s (%s ms)", operation.name, info,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - operation.start));
        }

        public Tracker end() {
            Operation operation = operations.remove(operations.size() - 1);
            logEnd(operation.name, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - operation.start));
            return this;
        }

        @Override
        public void close() {
            for (ListIterator<Operation> iterator = operations.listIterator(operations.size()); iterator.hasPrevious();) {
                Operation operation = (Operation) iterator.previous();
                logEnd(operation.name, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - operation.start));
                iterator.remove();
            }
        }

        private void logEnd(String info, long time) {
            BootstrapLogger.TRACKER_LOG.debugf("  END %s (%s ms)", info, time);
        }

        private static class Operation {

            private final String name;

            private final Long start;

            public Operation(String operation) {
                this.name = operation;
                this.start = System.nanoTime();
            }

        }

    }

}
