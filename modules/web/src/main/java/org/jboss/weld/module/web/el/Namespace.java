/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.web.el;

import java.util.HashMap;
import java.util.Map;

/**
 * A namespace for bean names
 *
 * @author Gavin King
 */
public class Namespace {
    private final String qualifiedName;
    private final String name;
    private final Map<String, Namespace> children;

    /**
     * Create a new namespace hierarchy
     *
     * @param namespace
     */
    public Namespace(Iterable<String> namespaces) {
        this(null, null);
        for (String namespace : namespaces) {
            String[] hierarchy = namespace.split("\\.");
            Namespace n = this;
            for (String s : hierarchy) {
                n = n.putIfAbsent(s);
            }
        }
    }

    protected Namespace(String name, String qualifiedName) {
        this.name = name;
        this.qualifiedName = qualifiedName;
        this.children = new HashMap<String, Namespace>();
    }

    private Namespace putIfAbsent(String key) {
        Namespace result = children.get(key);
        if (result == null) {
            result = new Namespace(key, qualifyName(key));
            children.put(key, result);
        }
        return result;
    }

    public Namespace get(String key) {
        return children.get(key);
    }

    public boolean contains(String key) {
        return children.containsKey(key);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    protected String getName() {
        return name;
    }

    public String qualifyName(String suffix) {
        return qualifiedName == null ? suffix : qualifiedName + "." + suffix;
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Namespace) {
            Namespace that = (Namespace) other;
            return this.getQualifiedName().equals(that.getQualifiedName());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Namespace(" + (name == null ? "Root" : name) + ')';
    }

    public void clear() {

    }

}
