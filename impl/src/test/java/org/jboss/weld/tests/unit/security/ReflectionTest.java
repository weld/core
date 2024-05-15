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
package org.jboss.weld.tests.unit.security;

import static org.junit.Assert.fail;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import org.jboss.weld.security.FieldLookupAction;
import org.jboss.weld.security.GetConstructorAction;
import org.jboss.weld.security.GetConstructorsAction;
import org.jboss.weld.security.GetDeclaredConstructorAction;
import org.jboss.weld.security.GetDeclaredConstructorsAction;
import org.jboss.weld.security.GetDeclaredFieldAction;
import org.jboss.weld.security.GetDeclaredFieldsAction;
import org.jboss.weld.security.GetDeclaredMethodAction;
import org.jboss.weld.security.GetDeclaredMethodsAction;
import org.jboss.weld.security.GetFieldAction;
import org.jboss.weld.security.GetFieldsAction;
import org.jboss.weld.security.GetMethodAction;
import org.jboss.weld.security.GetMethodsAction;
import org.jboss.weld.security.MethodLookupAction;
import org.jboss.weld.security.NewInstanceAction;
import org.jboss.weld.security.SetAccessibleAction;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionTest {

    @Test
    public void testGetField() throws PrivilegedActionException {
        Assert.assertNotNull(AccessController.doPrivileged(new GetFieldAction(TestObject.class, "publicField")));
    }

    @Test(expected = NoSuchFieldException.class)
    public void testGetFieldNotFound() throws Throwable {
        try {
            AccessController.doPrivileged(new GetFieldAction(TestObject.class, "eioota"));
        } catch (PrivilegedActionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testGetDeclaredField() throws PrivilegedActionException {
        Assert.assertNotNull(AccessController.doPrivileged(new GetDeclaredFieldAction(TestObject.class, "publicField")));
    }

    @Test(expected = NoSuchFieldException.class)
    public void testGetDeclaredFieldNotFound() throws Throwable {
        try {
            AccessController.doPrivileged(new GetDeclaredFieldAction(TestObject.class, "eioota"));
        } catch (PrivilegedActionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testGetFields() {
        Assert.assertEquals(1, AccessController.doPrivileged(new GetFieldsAction(TestObject.class)).length);
    }

    @Test
    public void testGetDeclaredFields() {
        Assert.assertEquals(2, AccessController.doPrivileged(new GetDeclaredFieldsAction(TestObject.class)).length);
    }

    @Test
    public void testFieldAccess() {
        testAllAccessible(grantAccess(AccessController.doPrivileged(new GetDeclaredFieldsAction(TestObject.class))));
    }

    private AccessibleObject[] grantAccess(AccessibleObject[] objects) {
        for (AccessibleObject object : objects) {
            AccessController.doPrivileged(SetAccessibleAction.of(object));
        }
        return objects;
    }

    private void testAllAccessible(AccessibleObject[] objects) {
        for (AccessibleObject object : objects) {
            if (!object.isAccessible()) {
                Assert.fail();
            }
        }
    }

    @Test
    public void testGetMethod() throws PrivilegedActionException {
        Assert.assertNotNull(AccessController
                .doPrivileged(new GetMethodAction(TestObject.class, "publicTest", new Class<?>[] { String.class })));
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetMethodNotFound() throws Throwable {
        try {
            AccessController
                    .doPrivileged(new GetMethodAction(TestObject.class, "xpublicTest", new Class<?>[] { String.class }));
        } catch (PrivilegedActionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testGetDeclaredMethod() throws PrivilegedActionException {
        Assert.assertNotNull(AccessController
                .doPrivileged(GetDeclaredMethodAction.of(TestObject.class, "publicTest", new Class<?>[] { String.class })));
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetDeclaredMethodNotFound() throws Throwable {
        try {
            AccessController
                    .doPrivileged(GetDeclaredMethodAction.of(TestObject.class, "xpublicTest", new Class<?>[] { String.class }));
        } catch (PrivilegedActionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testGetMethods() {
        Assert.assertEquals(10, AccessController.doPrivileged(new GetMethodsAction(TestObject.class)).length);
    }

    @Test
    public void testGetDeclaredMethods() {
        Assert.assertEquals(2, AccessController.doPrivileged(new GetDeclaredMethodsAction(TestObject.class)).length);
    }

    @Test
    public void testMethodAccess() {
        testAllAccessible(grantAccess(AccessController.doPrivileged(new GetDeclaredMethodsAction(TestObject.class))));
    }

    @Test
    public void testGetConstructor() throws PrivilegedActionException {
        Assert.assertNotNull(
                AccessController.doPrivileged(GetConstructorAction.of(TestObject.class, new Class<?>[] { Integer.class })));
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetConstructorNotFound() throws Throwable {
        try {
            AccessController.doPrivileged(GetConstructorAction.of(TestObject.class, new Class<?>[] { Float.class }));
        } catch (PrivilegedActionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testGetDeclaredConstructor() throws PrivilegedActionException {
        Assert.assertNotNull(AccessController
                .doPrivileged(GetDeclaredConstructorAction.of(TestObject.class, new Class<?>[] { String.class })));
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetDeclaredConstructorNotFound() throws Throwable {
        try {
            AccessController.doPrivileged(GetDeclaredConstructorAction.of(TestObject.class, new Class<?>[] { Float.class }));
        } catch (PrivilegedActionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testGetConstructors() {
        Assert.assertEquals(2, AccessController.doPrivileged(new GetConstructorsAction(TestObject.class)).length);
    }

    @Test
    public void testGetDeclaredConstructors() {
        Assert.assertEquals(3, AccessController.doPrivileged(new GetDeclaredConstructorsAction(TestObject.class)).length);
    }

    @Test
    public void testConstructorAccess() {
        testAllAccessible(grantAccess(AccessController.doPrivileged(new GetDeclaredConstructorsAction(TestObject.class))));
    }

    @Test
    public void testNewInstance() throws PrivilegedActionException {
        Assert.assertNotNull(AccessController.doPrivileged(
                NewInstanceAction.of(AccessController.doPrivileged(GetDeclaredConstructorAction.of(TestObject.class)))));
    }

    @Test
    public void testInvoke() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        TestObject to = new TestObject();
        Method m = TestObject.class.getMethod("publicTest", new Class<?>[] { String.class });
        Assert.assertEquals("foo", m.invoke(to, ""));
    }

    @Test
    public void testLookupMethod() throws PrivilegedActionException, NoSuchMethodException {
        Assert.assertNotNull(
                AccessController.doPrivileged(new MethodLookupAction(TestObject.class, "rootOfAllEvil", new Class<?>[] {})));
        Assert.assertNotNull(MethodLookupAction.lookupMethod(TestObject.class, "rootOfAllEvil", new Class<?>[] {}));
    }

    @Test
    public void testLookupMethodNotFound() throws Throwable {
        try {
            AccessController.doPrivileged(new MethodLookupAction(TestObject.class, "eioota", new Class<?>[] {}));
            fail();
        } catch (PrivilegedActionException e) {
            if (!(e.getCause() instanceof NoSuchMethodException)) {
                fail();
            }
        }
        try {
            MethodLookupAction.lookupMethod(TestObject.class, "eioota", new Class<?>[] {});
            fail();
        } catch (NoSuchMethodException e) {
        }
    }

    @Test
    public void testLookupField() throws PrivilegedActionException, NoSuchFieldException {
        Assert.assertNotNull(AccessController.doPrivileged(new FieldLookupAction(TestObject.class, "privateField")));
        Assert.assertNotNull(FieldLookupAction.lookupField(TestObject.class, "privateField"));
    }

    @Test
    public void testLookupFieldNotFound() throws Throwable {
        try {
            AccessController.doPrivileged(new FieldLookupAction(TestObject.class, "eioota"));
            fail();
        } catch (PrivilegedActionException e) {
            if (!(e.getCause() instanceof NoSuchFieldException)) {
                fail();
            }
        }
        try {
            FieldLookupAction.lookupField(TestObject.class, "eioota");
            fail();
        } catch (NoSuchFieldException e) {
        }
    }

}
