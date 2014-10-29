/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.test.beans.threading;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * An bean which implements Runnable and therefore can be run in a separate thread.
 * All such beans, when passed to Thread.start(), will be decorated by the
 * RunnableDecorator which will take care of making ThreadContext available to
 * that thread for resolution of @ThreadScoped beans.
 *
 * @author Peter Royle
 */
@Dependent
public class ThreadRunner implements Runnable {

    // an application scoped counter
    @Inject
    private SingletonCounter appCounter;
    // a thread scoped counter
    @Inject
    private ThreadCounter threadCounter;
    // a name for logging
    private String name = "Unnamed";
    // gather exceptions encountered for re-throwing in the test class
    private List<Exception> exceptions = new ArrayList<Exception>();

    /**
     * Run a loop, incrementing both the thread-scoped and application scoped
     * counters with each iteration.
     */
    public void run() {
        try {

            // Thread scoped counter should start at zero ...
            assertEquals(0, threadCounter.getCount());

            for (int loop = 1; loop <= ThreadContextTest.NUM_LOOPS; loop++) {
                final int appCount = appCounter.increment();
                final int threadCount = threadCounter.increment();
                assertEquals(loop, threadCount);
            }
            // ... and end at the number of loops
            assertEquals(ThreadContextTest.NUM_LOOPS, threadCounter.getCount());
        } catch (Exception e) {
            this.exceptions.add(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }
}
