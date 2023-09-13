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
package org.jboss.weld.util.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@link Multimap} whose collections of values are backed by a {@link List}.
 *
 * @author Martin Kouba
 *
 * @param <K>
 * @param <V>
 */
public class ListMultimap<K, V> extends AbstractMultimap<K, V, List<V>> {

    private static final long serialVersionUID = 6774436969456237682L;

    /**
     * Creates a new instance backed by a {@link HashMap} and {@link ArrayList}.
     */
    public ListMultimap() {
        this(HashMap::new, ArrayList::new, null);
    }

    /**
     * Creates a new instance backed by a {@link HashMap} and {@link ArrayList}. All key-value mappings are copied from the
     * input multimap.
     *
     * @param multimap
     */
    public ListMultimap(Multimap<K, V> multimap) {
        this(HashMap::new, ArrayList::new, multimap);
    }

    /**
     *
     * @param mapSupplier
     * @param collectionSupplier
     */
    public ListMultimap(Supplier<Map<K, List<V>>> mapSupplier, Supplier<List<V>> collectionSupplier, Multimap<K, V> multimap) {
        super(mapSupplier, collectionSupplier, multimap);
    }

}
