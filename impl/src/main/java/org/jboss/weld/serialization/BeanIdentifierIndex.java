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
package org.jboss.weld.serialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.bean.CommonBean;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.logging.SerializationLogger;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.collections.ImmutableMap;

/**
 * An optional per deployment service.
 *
 * The index holds identifiers for the specified set of beans (note that only instances of {@link CommonBean} and
 * implementations of {@link PassivationCapable}
 * are included). Identifiers are sorted into ascending order, according to the {@link BeanIdentifier#asString()} natural
 * ordering.
 *
 * @author Martin Kouba
 */
public class BeanIdentifierIndex implements Service {

    private volatile BeanIdentifier[] index;

    private volatile Map<BeanIdentifier, Integer> reverseIndex;

    private volatile Integer indexHash;

    private final AtomicBoolean indexBuilt = new AtomicBoolean(false);

    /**
     *
     * @param identifier
     * @return the position for the given bean identifier or null if the index does not contain the given identifier
     */
    public Integer getIndex(BeanIdentifier identifier) {
        checkIsBuilt();
        Preconditions.checkArgumentNotNull(identifier, "identifier");
        return reverseIndex.get(identifier);
    }

    /**
     *
     * @param idx
     * @return the identifier at the specified position
     */
    public BeanIdentifier getIdentifier(int idx) {
        checkIsBuilt();
        if (idx < 0 || idx >= index.length) {
            throw SerializationLogger.LOG.unableToGetBeanIdentifier(idx, getDebugInfo());
        }
        return index[idx];
    }

    /**
     * The index hash is used to detect possible inconsistencies in distributed environments.
     *
     * @return the index hash
     * @see Arrays#hashCode(Object[])
     */
    public Integer getIndexHash() {
        return indexHash;
    }

    /**
     * Note that the index can only be built once.
     *
     * @param beans The set of beans the index should be built from, only instances of {@link CommonBean} and implementations of
     *        {@link PassivationCapable} are
     *        included
     * @throws IllegalStateException If the index is built already
     */
    public void build(Set<Bean<?>> beans) {

        if (isBuilt()) {
            throw new IllegalStateException("BeanIdentifier index is already built!");
        }

        if (beans.isEmpty()) {
            index = new BeanIdentifier[0];
            reverseIndex = Collections.emptyMap();
            indexHash = 0;
            indexBuilt.set(true);
            return;
        }

        List<BeanIdentifier> tempIndex = new ArrayList<BeanIdentifier>(beans.size());

        for (Bean<?> bean : beans) {
            if (bean instanceof CommonBean<?>) {
                tempIndex.add(((CommonBean<?>) bean).getIdentifier());
            } else if (bean instanceof PassivationCapable) {
                tempIndex.add(new StringBeanIdentifier(((PassivationCapable) bean).getId()));
            }
        }

        Collections.sort(tempIndex, new Comparator<BeanIdentifier>() {
            @Override
            public int compare(BeanIdentifier o1, BeanIdentifier o2) {
                return o1.asString().compareTo(o2.asString());
            }
        });

        index = tempIndex.toArray(new BeanIdentifier[tempIndex.size()]);

        ImmutableMap.Builder<BeanIdentifier, Integer> builder = ImmutableMap.builder();
        for (int i = 0; i < index.length; i++) {
            builder.put(index[i], i);
        }
        reverseIndex = builder.build();

        indexHash = Arrays.hashCode(index);

        if (BootstrapLogger.LOG.isDebugEnabled()) {
            BootstrapLogger.LOG.beanIdentifierIndexBuilt(getDebugInfo());
        }
        indexBuilt.set(true);
    }

    /**
     *
     * @return <code>true</code> if the index is built, <code>false</code> otherwise
     */
    public boolean isBuilt() {
        return indexBuilt.get();
    }

    /**
     * @return <code>true</code> if the index is empty, <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return index.length == 0;
    }

    @Override
    public void cleanup() {
        index = null;
    }

    private void checkIsBuilt() {
        if (!isBuilt()) {
            throw new IllegalStateException("BeanIdentifier index not built!");
        }
    }

    @Override
    public String toString() {
        return String.format("BeanIdentifierIndex [hash=%s, indexed=%s]", indexHash, index.length);
    }

    public String getDebugInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append(toString());
        builder.append(" \n");
        for (int i = 0; i < index.length; i++) {
            builder.append("  ");
            builder.append(String.format("%4d", i));
            builder.append(": ");
            builder.append(index[i]);
            builder.append("\n");
        }
        return builder.toString();
    }

}