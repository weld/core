/*
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

package org.jboss.weld.interceptor.spi.model;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public enum InterceptionType {

    AROUND_INVOKE(false, "javax.interceptor.AroundInvoke"),
    AROUND_TIMEOUT(false, "javax.interceptor.AroundTimeout"),
    POST_CONSTRUCT(true, "javax.annotation.PostConstruct"),
    PRE_DESTROY(true, "javax.annotation.PreDestroy"),
    POST_ACTIVATE(true, "javax.ejb.PostActivate"),
    PRE_PASSIVATE(true, "javax.ejb.PrePassivate"),
    AROUND_CONSTRUCT(true, "javax.interceptor.AroundConstruct");

    private final boolean lifecycleCallback;

    private final String annotationClassName;

    InterceptionType(boolean lifecycleCallback, String annotationClassName) {
        this.lifecycleCallback = lifecycleCallback;
        this.annotationClassName = annotationClassName;
    }

    public boolean isLifecycleCallback() {
        return lifecycleCallback;
    }

    public String annotationClassName() {
        return annotationClassName;
    }

    public static InterceptionType valueOf(javax.enterprise.inject.spi.InterceptionType interceptionType) {
        return valueOf(interceptionType.name());
    }
}
