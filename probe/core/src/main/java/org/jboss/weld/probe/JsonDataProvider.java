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
package org.jboss.weld.probe;

import javax.enterprise.inject.Vetoed;
import javax.management.MXBean;

import org.jboss.weld.probe.ProbeDynamicMBean.Description;
import org.jboss.weld.probe.ProbeDynamicMBean.ParamName;

/**
 * A component which loads JSON data for a specific {@link Resource}. This interface also represents a MXBean which allows to access Probe JSON data via JMX.
 *
 * @author Martin Kouba
 */
@Vetoed
@MXBean
public interface JsonDataProvider {

    /**
     *
     * @return the JSON data
     * @see Resource#DEPLOYMENT
     */
    @Description("Receives a deployment info.")
    String receiveDeployment();

    /**
     *
     * @param pageIndex
     * @param pageSize
     * @param filters
     * @param representation
     * @return the JSON data
     * @see Resource#BEANS
     */
    @Description("Receives a collection of beans.")
    String receiveBeans(@ParamName("pageIndex") int pageIndex, @ParamName("pageSize") int pageSize,
            @ParamName("filters") @Description("A blank-separated list of key-value pairs. Keys and values are separated by a colon. E.g beanClass:\"Foo\" scope:\"myScope\".") String filters,
            @ParamName("representation") String representation);

    /**
     *
     * @param id
     * @return the JSON data
     * @see Resource#BEAN
     */
    @Description("Receives a bean detail.")
    String receiveBean(@ParamName("id") String id, @ParamName("transientDependencies") boolean transientDependencies,
            @ParamName("transientDependents") boolean transientDependents);

    /**
     *
     * @param id
     * @return the JSON data
     * @see Resource#BEAN_INSTANCE
     */
    @Description("Receives a contextual instance of a bean. This is only supported for a limited set of scopes.")
    String receiveBeanInstance(@ParamName("id") String id);

    /**
     *
     * @param pageIndex
     * @param pageSize
     * @param filters
     * @param representation
     * @return the JSON data
     * @see Resource#OBSERVERS
     */
    @Description("Receives a collection of observer methods.")
    String receiveObservers(@ParamName("pageIndex") int pageIndex, @ParamName("pageSize") int pageSize,
            @ParamName("filters") @Description("A blank-separated list of key-value pairs. Keys and values are separated by a colon. E.g beanClass:\"Foo\" qualifier:\"any\".") String filters,
            @ParamName("representation") String representation);

    /**
     *
     * @param id
     * @return the JSON data
     * @see Resource#OBSERVER
     */
    @Description("Receives an observer method detail.")
    String receiveObserver(@ParamName("id") String id);

    /**
     *
     * @return the JSON data
     * @see Resource#CONTEXTS
     */
    @Description("Receives a collection of inspectable contexts.")
    String receiveContexts();

    /**
     *
     * @param id
     * @return the JSON data
     * @see Resource#CONTEXT
     */
    @Description("Receives a collection of contextual instances for the given inspectable context.")
    String receiveContext(@ParamName("id") String id);

    /**
     *
     * @param pageIndex
     * @param pageSize
     * @param filters
     * @param representation
     * @return the JSON data
     * @see Resource#INVOCATIONS
     */
    @Description("Receives a collection of invocation trees.")
    String receiveInvocations(@ParamName("pageIndex") int pageIndex, @ParamName("pageSize") int pageSize,
            @ParamName("filters") @Description("A blank-separated list of key-value pairs. Keys and values are separated by a colon. E.g beanClass:\"Foo\" description:\"bar\".") String filters,
            @ParamName("representation") String representation);

    /**
     *
     * @return the JSON result
     * @see Resource#INVOCATIONS
     */
    @Description("Removes all monitoring data - invocation trees.")
    String clearInvocations();

    /**
     *
     * @param id
     * @return the JSON data
     * @see Resource#INVOCATION
     */
    @Description("Receives an invocation tree detail.")
    String receiveInvocation(@ParamName("id") String id);

    /**
     *
     * @param pageIndex
     * @param pageSize
     * @param filters
     * @return the JSON data
     * @see Resource#EVENTS
     */
    @Description("Receives a collection of fired events.")
    String receiveEvents(@ParamName("pageIndex") int pageIndex, @ParamName("pageSize") int pageSize,
            @ParamName("filters") @Description("A blank-separated list of key-value pairs. Keys and values are separated by a colon. E.g beanClass:\"Foo\" description:\"bar\".") String filters);

    /**
     *
     * @return the JSON result
     * @see Resource#EVENTS
     */
    @Description("Removes all monitoring data - fired events.")
    String clearEvents();

    /**
     *
     * @return the JSON result
     * @see Resource#MONITORING_STATS
     */
    @Description("Receives monitoring stats.")
    String receiveMonitoringStats();

    /**
     *
     * @param pageIndex
     * @param pageSize
     * @param filters
     * @param representation
     * @return the JSON data
     * @see Resource#AVAILABLE_BEANS
     */
    @Description("Receives a collection of beans availabe in a specific bean deployment archive.")
    String receiveAvailableBeans(@ParamName("pageIndex") int pageIndex, @ParamName("pageSize") int pageSize,
            @ParamName("filters") @Description("A blank-separated list of key-value pairs. Keys and values are separated by a colon. E.g requiredType:\"com.foo.Bar\" resolve:false.") String filters,
            @ParamName("representation") String representation);

}
