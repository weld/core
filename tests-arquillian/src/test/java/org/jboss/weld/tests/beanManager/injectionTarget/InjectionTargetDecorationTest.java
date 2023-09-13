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
package org.jboss.weld.tests.beanManager.injectionTarget;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InjectionTargetDecorationTest {

    @Inject
    private BeanManagerImpl manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InjectionTargetDecorationTest.class))
                .decorate(BuildingDecorator1.class, BuildingDecorator2.class)
                .addPackage(InjectionTargetDecorationTest.class.getPackage());
    }

    @Test
    public void testInjectionTargetDecoration() {
        @SuppressWarnings("unchecked")
        Bean<Skyscraper> bean = (Bean<Skyscraper>) manager.resolve(manager.getBeans(Skyscraper.class));
        AnnotatedType<Skyscraper> type = manager.createAnnotatedType(Skyscraper.class);

        InjectionTarget<Skyscraper> target = manager.getLocalInjectionTargetFactory(type).createInjectionTarget(bean);
        CreationalContext<Skyscraper> ctx = manager.createCreationalContext(bean);

        Skyscraper instance = target.produce(ctx);

        assertTrue(instance.decorated1());
        assertTrue(instance.decorated2());
    }

    @Test
    public void testInjectionTargetMayBeCreatedForAbstractClass() {
        InjectionTarget<AbstractClass> it = manager.getInjectionTargetFactory(manager.createAnnotatedType(AbstractClass.class))
                .createInjectionTarget(null);
        ConcreteClass instance = new ConcreteClass();
        it.inject(instance, manager.<AbstractClass> createCreationalContext(null));
    }
}
