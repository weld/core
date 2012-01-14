/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.util.Beans;
import org.junit.Test;

public class BeansTest {

    private static final String SIGNATURE = "[java.lang.Object,java.lang.Object,java.util.List<java.lang.String>,java.util.Map<java.lang.Integer,java.lang.String>,java.util.Map<java.lang.Integer,java.lang.String>,javax.enterprise.inject.Instance<java.lang.Integer>]";
    
    @SuppressWarnings("serial")
    @Test
    public void testTypeCollectionSignature() {
        Collection<Type> types = new ArrayList<Type>();
        types.add(Object.class);
        types.add(new TypeLiteral<List<String>>() {
        }.getType());
        types.add(new TypeLiteral<Map<Integer, String>>() {
        }.getType());
        types.add(new TypeLiteral<Map<Integer, String>>() {
        }.getType());
        types.add(new TypeLiteral<Instance<Integer>[]>() {
        }.getType());
        types.add(Object.class);
        assertEquals(SIGNATURE, Beans.createTypeCollectionId(types));
    }
}
