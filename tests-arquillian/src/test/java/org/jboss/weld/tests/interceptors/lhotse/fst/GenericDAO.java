/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.weld.tests.interceptors.lhotse.fst;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@ApplicationScoped
public abstract class GenericDAO<T extends Entity> implements DAO<T> {
    public abstract Class<T> entityClass();

    @Tx
    public boolean save(T t) {
        System.out.println("t = " + t);
        return true;
    }

    @Tx(1)
    public T find(Long id) {
        return find(entityClass(), id);
    }

    @Tx(1)
    public <U> U find(Class<U> clazz, Long id) {
        if (clazz == null)
            throw new IllegalArgumentException("Null clazz");
        if (id == null)
            throw new IllegalArgumentException("Null id");

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
