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
package org.jboss.weld.context.beanstore.http;

import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.util.collections.EnumerationList;
import org.jboss.weld.util.reflection.Reflections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This session bean store knows how to handle cyclic bean creation.
 *
 * @author Ales Justin
 */
public class LazyCyclicSessionBeanStore extends LazySessionBeanStore {

    private static ThreadLocal<Map<String, Object>> temp = new ThreadLocal<Map<String, Object>>();

    public LazyCyclicSessionBeanStore(HttpServletRequest request, NamingScheme namingScheme) {
        super(request, namingScheme);
    }

    @Override
    protected HttpSession getSession(boolean create) {
        HttpSession session = null;

        Map<String, Object> map = null;
        // only put temp if we're to create new session
        if (create) {
            map = new HashMap<String, Object>();
            temp.set(map);
        }

        try {
            session = super.getSession(create);
            return session;
        } finally {
            if (create) {
                temp.remove();

                if (session != null && map.isEmpty() == false) {
                    for (Map.Entry<String, Object> entry : map.entrySet())
                        session.setAttribute(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    protected Collection<String> getAttributeNames() {
        Map<String, Object> map = temp.get();
        if (map != null) {
            Collection<String> names = new HashSet<String>();
            names.addAll(map.keySet());
            HttpSession session = getSessionIfExists();
            if (session != null)
                names.addAll(new EnumerationList<String>(Reflections.<Enumeration<String>>cast(session.getAttributeNames())));
            return names;
        }

        return super.getAttributeNames();
    }

    @Override
    protected void removeAttribute(String key) {
        Map<String, Object> map = temp.get();
        if (map != null) {
            map.remove(key);

            HttpSession session = getSessionIfExists();
            if (session != null)
                session.removeAttribute(key);
        } else {
            super.removeAttribute(key);
        }
    }

    @Override
    protected void setAttribute(String key, Object instance) {
        Map<String, Object> map = temp.get();
        if (map != null) {
            map.put(key, instance);
        } else {
            super.setAttribute(key, instance);
        }
    }

    @Override
    protected Object getAttribute(String prefixedId) {
        Map<String, Object> map = temp.get();
        if (map != null) {
            Object value = map.get(prefixedId);
            if (value != null)
                return value;

            HttpSession session = getSessionIfExists();
            if (session != null)
                return session.getAttribute(prefixedId);

            return null;
        }

        return super.getAttribute(prefixedId);
    }
}
