/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.inheritance.generic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class MemberLevelInheritanceTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(MemberLevelInheritanceTest.class))
                .addPackage(Dao.class.getPackage());
    }

    @Inject
    private BeanManager manager;

    @Test
    public void testFieldInjectionPoint(UserDao userDao, CarDao carDao) {
        assertNotNull(userDao.getField());
        assertNotNull(carDao.getField());
    }

    @Test
    public void testInitializerInjectionPoint(UserDao userDao, CarDao carDao) {
        assertNotNull(userDao.getInitializerParameter());
        assertNotNull(carDao.getInitializerParameter());
    }

    @Test
    public void testObserverMethods(UserDao userDao, CarDao carDao) {
        manager.getEvent().select(User.class).fire(new User());
        manager.getEvent().select(Car.class).fire(new Car());

        assertNotNull(userDao.getEvent());
        assertNotNull(userDao.getObserverInjectionPoint());
        assertNotNull(carDao.getEvent());
        assertNotNull(carDao.getObserverInjectionPoint());
    }
    //    @Test
    //    public void testInjectionPoint(BeanManager manager) throws Exception {
    //
    //        Bean<Foo> fooBean = Reflections.cast(manager.resolve(manager.getBeans(Foo.class)));
    //        Set<InjectionPoint> injectionPoints = fooBean.getInjectionPoints();
    //        assertEquals(injectionPoints.size(), 1);
    //        InjectionPoint inheritedInjectionPoint = injectionPoints.iterator().next();
    //
    //        checkParameterizedType(inheritedInjectionPoint.getType(), Baz.class, String.class);
    //    }

    //    @Test
    //    public void testObserver(BeanManager manager) throws Exception {
    //
    //        Set<ObserverMethod<? super Qux>> observerMethods = manager.resolveObserverMethods(new Qux(null));
    //        // Foo and Bar
    //        assertEquals(observerMethods.size(), 2);
    //
    //        for (ObserverMethod<? super Qux> observerMethod : observerMethods) {
    //            if (observerMethod.getBeanClass().equals(Foo.class)) {
    //                checkParameterizedType(observerMethod.getObservedType(), Baz.class, String.class);
    //                return;
    //            }
    //        }
    //        // No Foo observer
    //        Assert.fail();
    //    }

    private void checkParameterizedType(Type declaredType, Type rawType, Type argumentType) {

        assertTrue(declaredType instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) declaredType;

        assertEquals(parameterizedType.getRawType(), rawType);

        Type[] arguments = parameterizedType.getActualTypeArguments();
        assertEquals(arguments.length, 1);
        assertTrue(arguments[0].equals(argumentType));
    }

}
