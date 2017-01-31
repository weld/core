/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.alternatives.weld2313;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class VetoedAlternativeTest {
    @Inject
    private BeanManagerImpl beanManager;

    @Deployment
    public static WebArchive createTestArchive() {
        BeansXml beansXml = new BeansXml().alternatives(MockPaymentProcessorImpl.class).stereotype(AlternativeConsumerStereotype.class);
        WebArchive testDeployment = ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(VetoedAlternativeTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(VetoedAlternativeTest.class.getPackage())
                .addAsServiceProvider(Extension.class, VetoingExtension.class)
                .addAsWebInfResource(beansXml, "beans.xml")
                .addAsResource(PropertiesBuilder.newBuilder().set(ConfigurationKey.ALLOW_VETOED_ALTERNATIVES.get(), "true").build(),"weld.properties");
        return testDeployment;
    }

    @Test
    public void mockAlternativeIsVetoed() {
        Assert.assertEquals("paymentProcessorImpl", getUniqueBean(PaymentProcessor.class).getName());
    }

    @Test
    public void alternativeStereotypeIsVetoed() {
        Assert.assertEquals(RequestScoped.class, getUniqueBean(Consumer.class).getScope());
    }

    @Test
    public void testWeldConfiguration() {
        WeldConfiguration configuration =  beanManager.getServices().get(WeldConfiguration.class);
        Assert.assertTrue(configuration.getBooleanProperty(ConfigurationKey.ALLOW_VETOED_ALTERNATIVES));
    } 

    private Bean<?> getUniqueBean(Class type) {
        Set<Bean<?>> beans = beanManager.getBeans(type);
        return resolveUniqueBean(type, beans);
    }

    private Bean<?> resolveUniqueBean(Type type, Set<Bean<?>> beans) {
        if (beans.size() == 0) {
            throw new UnsatisfiedResolutionException("Unable to resolve any beans of " + type);
        } else if (beans.size() > 1) {
            throw new AmbiguousResolutionException("More than one bean available (" + beans + ")");
        }
        return beans.iterator().next();
    }

}
