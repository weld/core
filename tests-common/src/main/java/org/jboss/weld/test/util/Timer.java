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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Delays thread execution for specified time or unless stop conditions are satisfied according to the actual
 * {@link ResolutionLogic}. This class is not
 * thread-safe.
 * <p>
 * Setting the sleep interval to the value less than 15 ms is questionable since some operating systems do not provide such
 * precision. Moreover such values may
 * impact test performance.
 * </p>
 * <p>
 * In case of no stop conditions are specified (not recommended), the timer logic corresponds to the regular
 * {@link Thread#sleep(long)} execution.
 * </p>
 */
public class Timer {

    private static final long DEFAULT_SLEEP_INTERVAL = 50L;
    private static final int DEFAULT_CAPACITY = 5;

    private static final ResolutionLogic DEFAULT_RESOLUTION_LOGIC = ResolutionLogic.DISJUNCTION;

    /**
     * Delay in ms
     */
    private long delay;

    /**
     * Thread sleep interval
     */
    private long sleepInterval;

    /**
     * Stop conditions
     */
    private List<StopCondition> stopConditions = null;

    /**
     * Stop conditions resolution logic
     */
    private ResolutionLogic resolutionLogic;

    private boolean stopConditionsSatisfiedBeforeTimeout;

    /**
     * Create new timer with default delay, sleep interval and stop conditions resolution logic.
     */
    public Timer() {
        reset();
    }

    /**
     * Set the delay value. The value is automatically adjusted according to the {@link Configuration#getTestTimeoutFactor()} so
     * that it's possible to configure
     * timeouts according to the testing runtime performance and throughput.
     *
     * @param delay The delay in milliseconds
     * @return self
     */
    public Timer setDelay(long delay) {
        return setDelay(delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Set the delay value. The value is automatically adjusted according to the {@link Configuration#getTestTimeoutFactor()} so
     * that it's possible to configure
     * timeouts according to the testing runtime performance and throughput.
     *
     * @param delay
     * @param timeUnit
     * @return self
     */
    public Timer setDelay(long delay, TimeUnit timeUnit) {
        if (delay <= 0) {
            throw new IllegalArgumentException("Delay must be greater than zero");
        }
        this.delay = timeUnit.toMillis(delay);
        return this;
    }

    /**
     * Set new sleep interval value.
     *
     * @param sleepInterval
     * @return self
     */
    public Timer setSleepInterval(long sleepInterval) {
        this.sleepInterval = sleepInterval;
        return this;
    }

    /**
     * Set new resolution logic.
     *
     * @param resolutionLogic
     * @return self
     */
    public Timer setResolutionLogic(ResolutionLogic resolutionLogic) {
        this.resolutionLogic = resolutionLogic;
        return this;
    }

    /**
     * Add new stop condition.
     *
     * @param condition
     * @return self
     */
    public Timer addStopCondition(StopCondition condition) {
        return addStopCondition(condition, false);
    }

    /**
     * Add new stop condition.
     *
     * @param condition
     * @param clear Clear stop conditions and reset {@link #stopConditionsSatisfiedBeforeTimeout}
     * @return self
     */
    public Timer addStopCondition(StopCondition condition, boolean clear) {

        if (condition != null) {

            if (stopConditions == null) {
                stopConditions = new ArrayList<StopCondition>(DEFAULT_CAPACITY);
            } else if (clear) {
                clearStopConditions();
            }
            stopConditions.add(condition);
        }
        return this;
    }

    /**
     * Start the timer.
     *
     * @throws InterruptedException
     */
    public Timer start() throws InterruptedException {

        checkConfiguration();

        if (stopConditions == null || stopConditions.isEmpty()) {
            Thread.sleep(delay);
        } else {

            long start = System.currentTimeMillis();

            while (resolveSleepConditions(start)) {
                Thread.sleep(sleepInterval);
            }
        }
        return this;
    }

    /**
     * Reset to default values.
     */
    public void reset() {
        this.delay = -1;
        this.sleepInterval = DEFAULT_SLEEP_INTERVAL;
        this.resolutionLogic = DEFAULT_RESOLUTION_LOGIC;
        clearStopConditions();
    }

    /**
     * Clear stop conditions and reset {@link #stopConditionsSatisfiedBeforeTimeout}.
     */
    public void clearStopConditions() {
        if (stopConditions != null) {
            stopConditions.clear();
        }
        this.stopConditionsSatisfiedBeforeTimeout = false;
    }

    /**
     * @return <code>true</code> if stop conditions are satisfied according to actual {@link #resolutionLogic} before timeout
     *         occurs, <code>false</code>
     *         otherwise
     */
    public boolean isStopConditionsSatisfiedBeforeTimeout() {
        return stopConditionsSatisfiedBeforeTimeout;
    }

    /**
     * @return the current delay in ms
     */
    public long getDelay() {
        return delay;
    }

    /**
     * @return the current sleep interval in ms
     */
    public long getSleepInterval() {
        return sleepInterval;
    }

    /**
     * Start a new timer with specified delay.
     *
     * @param delay
     * @return finished timer
     * @throws InterruptedException
     */
    public static Timer startNew(long delay) throws InterruptedException {
        return new Timer().setDelay(delay).start();
    }

    /**
     * Start a new timer with specified delay and sleep interval.
     *
     * @param delay
     * @return finished timer
     * @throws InterruptedException
     */
    public static Timer startNew(long delay, long sleepInterval) throws InterruptedException {
        return new Timer().setDelay(delay).setSleepInterval(sleepInterval).start();
    }

    private boolean resolveSleepConditions(long start) {

        if (hasConditionsSatisfied()) {
            stopConditionsSatisfiedBeforeTimeout = true;
            return false;
        }
        if (isTimeoutExpired(start)) {
            return false;
        }
        return true;
    }

    private void checkConfiguration() {
        if (delay < 0 || sleepInterval < 0 || delay < sleepInterval) {
            throw new IllegalStateException("Invalid timer configuration");
        }
    }

    private boolean hasConditionsSatisfied() {
        switch (resolutionLogic) {
            case DISJUNCTION:
                return hasAtLeastOneConditionsSatisfied();
            case CONJUNCTION:
                return hasAllConditionsSatisfied();
            default:
                throw new IllegalStateException("Unsupported condition resolution logic");
        }
    }

    private boolean hasAtLeastOneConditionsSatisfied() {
        for (StopCondition condition : stopConditions) {
            if (condition.isSatisfied()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAllConditionsSatisfied() {
        for (StopCondition condition : stopConditions) {
            if (!condition.isSatisfied()) {
                return false;
            }
        }
        return true;
    }

    private boolean isTimeoutExpired(long start) {
        return (System.currentTimeMillis() - start) >= delay;
    }

    /**
     *
     */
    public interface StopCondition {

        /**
         * @return <code>true</code> if stop condition satisfied, <code>false</code> otherwise
         */
        boolean isSatisfied();

    }

    public static enum ResolutionLogic {

        /**
         * At least one condition must be satisfied
         */
        DISJUNCTION,
        /**
         * All conditions must be satisfied
         */
        CONJUNCTION,;

    }

}