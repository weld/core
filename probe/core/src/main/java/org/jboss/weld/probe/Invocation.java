/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.interceptor.InvocationContext;

/**
 * Information about a single business method invocation.
 * <p>
 * Note we can't always detect the type reliably.
 *
 * @author Martin Kouba
 */
@Vetoed
public final class Invocation {

    /**
     * A unique idx of an entry point (optional)
     */
    private final Integer entryPointIdx;

    private final Bean<?> interceptedBean;

    /**
     * If there is no intercepted bean we use the name of the declaring class
     */
    private final String declaringClassName;

    /**
     * Start time in ms
     */
    private final long start;

    /**
     * Duration in ns
     */
    private final long duration;

    private final String methodName;

    private final List<Invocation> children;

    private final Type type;

    private final String description;

    /**
     *
     * @param entryPointIdx
     * @param isEntryPoint
     * @param interceptedBean
     * @param declaringClassName
     * @param start
     * @param duration
     * @param methodName
     * @param children
     * @param type
     * @param description
     */
    public Invocation(Integer entryPointIdx, Bean<?> interceptedBean, String declaringClassName, long start, long duration, String methodName,
            List<Invocation> children, Type type, String description) {
        this.entryPointIdx = entryPointIdx;
        this.interceptedBean = interceptedBean;
        this.declaringClassName = declaringClassName;
        this.start = start;
        this.duration = duration;
        this.methodName = methodName;
        this.children = children;
        this.type = type;
        this.description = description;
    }

    public Integer getEntryPointIdx() {
        return entryPointIdx;
    }

    public boolean isEntryPoint() {
        return entryPointIdx != null;
    }

    public Bean<?> getInterceptedBean() {
        return interceptedBean;
    }

    public String getDeclaringClassName() {
        return declaringClassName;
    }

    public String getBeanClass() {
        return interceptedBean != null ? interceptedBean.getBeanClass().getName() : declaringClassName;
    }

    public long getStart() {
        return start;
    }

    public long getDuration() {
        return duration;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Invocation> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public enum Type {

        BUSINESS, PRODUCER, DISPOSER, OBSERVER, CONSTRUCTOR

    }

    static enum Comparators implements Comparator<Invocation> {

        /**
         * Note that this can only be used for entry points!
         */
        ENTRY_POINT_IDX {
            @Override
            public int compare(Invocation o1, Invocation o2) {
                return o2.getEntryPointIdx().compareTo(o1.getEntryPointIdx());
            }
        },
        START {
            @Override
            public int compare(Invocation o1, Invocation o2) {
                return (o1.getStart() < o2.getStart()) ? 1 : ((o1.getStart() == o2.getStart()) ? 0 : -1);
            }
        },
        START_AND_DURATION {
            @Override
            public int compare(Invocation o1, Invocation o2) {
                if (o1.getStart() == o2.getStart()) {
                    return (o1.getDuration() < o2.getDuration()) ? 1 : ((o1.getDuration() == o2.getDuration()) ? 0 : -1);
                }
                return (o1.getStart() < o2.getStart()) ? 1 : ((o1.getStart() == o2.getStart()) ? 0 : -1);
            }
        };

        public abstract int compare(Invocation o1, Invocation o2);

    }

    /**
     * This builder is not thread-safe.
     *
     * @author Martin Kouba
     */
    static class Builder {

        private Integer entryPointIdx;

        private Bean<?> interceptedBean;

        private String declaringClassName;

        private long start;

        private long duration;

        private String methodName;

        private List<Builder> children;

        private Type type;

        private String description;

        private Builder parent;

        private boolean ignoreIfNoChildren;

        static Builder newBuilder(Integer idx) {
            return new Builder(idx);
        }

        Builder newChild() {
            Invocation.Builder child = newBuilder(null);
            addChild(child);
            return child;
        }

        private Builder(Integer idx) {
            this.entryPointIdx = idx;
        }

        boolean isEntryPoint() {
            return this.entryPointIdx != null;
        }

        Builder setInterceptedBean(Bean<?> bean) {
            this.interceptedBean = bean;
            return this;
        }

        Builder setDeclaringClassName(String declaringClassName) {
            this.declaringClassName = declaringClassName;
            return this;
        }

        Builder setStart(long start) {
            this.start = start;
            return this;
        }

        Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        Builder setMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        Builder setType(Type type) {
            this.type = type;
            return this;
        }

        Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public boolean isIgnored() {
            return ignoreIfNoChildren && !hasChildren();
        }

        Builder ignoreIfNoChildren() {
            this.ignoreIfNoChildren = true;
            return this;
        }

        Builder guessType(InvocationContext ctx) {
            // This will only work for "unmodified" discovered types
            if (ctx.getMethod().isAnnotationPresent(Produces.class)) {
                return setType(Type.PRODUCER);
            } else {
                Annotation[][] parameterAnnotations = ctx.getMethod().getParameterAnnotations();
                if (parameterAnnotations.length > 0) {
                    for (Annotation[] annotations : parameterAnnotations) {
                        for (Annotation annotation : annotations) {
                            Class<? extends Annotation> type = annotation.annotationType();
                            if (Observes.class.equals(type)) {
                                return setType(Type.OBSERVER);
                            } else if (Disposes.class.equals(type)) {
                                return setType(Type.DISPOSER);
                            }
                        }
                    }
                }
            }
            return setType(Type.BUSINESS);
        }

        Builder getParent() {
            return parent;
        }

        void setParent(Builder parent) {
            this.parent = parent;
        }

        boolean addChild(Builder child) {
            if (children == null) {
                children = new ArrayList<Invocation.Builder>();
            }
            child.setParent(this);
            return children.add(child);
        }

        public boolean hasChildren() {
            return children != null && !children.isEmpty();
        }

        public Integer getEntryPointIdx() {
            return entryPointIdx;
        }

        Invocation build() {
            List<Invocation> invocations = null;
            if (children != null) {
                invocations = new ArrayList<Invocation>(children.size());
                for (Builder builder : children) {
                    invocations.add(builder.build());
                }
            }
            return new Invocation(entryPointIdx, interceptedBean, declaringClassName, start, duration, methodName, invocations, type, description);
        }

    }

}
