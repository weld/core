/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.environment.osgi.tests.service;

import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.jboss.weld.osgi.tests.bundle1.api.PersonalizedHashCodeService;
import org.jboss.weld.osgi.tests.bundle1.api.PropertyService;
import org.jboss.weld.osgi.tests.bundle1.util.ServiceProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class ServiceConsumingTest {

    @Configuration
    public static Option[] configure() {
        return options(
                Environment.CDIOSGiEnvironment(
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle1").version("1.2.0-SNAPSHOT"),
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle2").version("1.2.0-SNAPSHOT"),
                        mavenBundle("org.jboss.weld.osgi.tests", "weld-osgi-bundle3").version("1.2.0-SNAPSHOT")
                )
        );
    }

    @Test
    //@Ignore
    public void serviceOSGiConsumingTest(BundleContext context) throws InterruptedException, InvalidSyntaxException {
        Environment.waitForEnvironment(context);

        ServiceReference[] propertyServiceReferences = context.getServiceReferences(PropertyService.class.getName(), null);
        Assert.assertNotNull("The property service reference array was null", propertyServiceReferences);
        Assert.assertEquals("The number of service property service implementations was wrong", 3, propertyServiceReferences.length);

        ServiceReference[] propertyService1References = context.getServiceReferences(PropertyService.class.getName(), "(name.value=1)");
        Assert.assertNotNull("The property service 1 reference array was null", propertyService1References);
        Assert.assertEquals("The number of service 1 property service implementations was wrong", 1, propertyService1References.length);
        PropertyService service1 = (PropertyService) context.getService(propertyService1References[0]);
        Assert.assertNotNull("The service 1 was null", service1);
        Assert.assertEquals("The service 1 method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", service1.whoAmI());

        ServiceReference[] propertyService2References = context.getServiceReferences(PropertyService.class.getName(), "(name.value=2)");
        Assert.assertNotNull("The property service 2 reference array was null", propertyService2References);
        Assert.assertEquals("The number of service 2 property service implementations was wrong", 1, propertyService2References.length);
        PropertyService service2 = (PropertyService) context.getService(propertyService2References[0]);
        Assert.assertNotNull("The service 3 was null", service2);
        Assert.assertEquals("The service 3 method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", service2.whoAmI());
    }

    @Test
    //@Ignore
    public void serviceCDIConsumingTest(BundleContext context) throws InterruptedException, InvalidSyntaxException {
        Environment.waitForEnvironment(context);

        ServiceReference[] serviceProviderReferences = context.getServiceReferences(ServiceProvider.class.getName(), null);
        Assert.assertNotNull("The service provider reference array was null", serviceProviderReferences);
        Assert.assertEquals("The number of service provider implementations was wrong", 1, serviceProviderReferences.length);
        ServiceProvider provider = (ServiceProvider) context.getService(serviceProviderReferences[0]);
        Assert.assertNotNull("The service provider was null", provider);

//        PropertyService service = provider.getService();
//        Assert.assertNotNull("The service was null", service);
//        Assert.assertEquals("The service method result was wrong","org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl1",service.whoAmI());
        PropertyService filteredService = provider.getFilteredService();
        Assert.assertNotNull("The filtered service was null", filteredService);
        Assert.assertEquals("The filtered service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", filteredService.whoAmI());
        PropertyService qualifiedService = provider.getQualifiedService();
        Assert.assertNotNull("The qualified service was null", qualifiedService);
        Assert.assertEquals("The qualified service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", qualifiedService.whoAmI());
        PropertyService filteredFromQualifierService = provider.getFilteredFromQualifierService();
        Assert.assertNotNull("The filtered from qualifier service was null", filteredFromQualifierService);
        Assert.assertEquals("The filtered from qualifier service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", filteredFromQualifierService.whoAmI());
        PropertyService qualifiedFromPropertyService = provider.getQualifiedFromPropertyService();
        Assert.assertNotNull("The qualified from property service was null", qualifiedFromPropertyService);
        Assert.assertEquals("The qualified from property service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", qualifiedFromPropertyService.whoAmI());
        PropertyService otherFilteredService = provider.getOtherFilteredService();
        Assert.assertNotNull("The other filtered service was null", otherFilteredService);
        Assert.assertEquals("The other filtered service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", otherFilteredService.whoAmI());

//        PropertyService constructorService = provider.getConstructorService();
//        Assert.assertNotNull("The constructor service was null", constructorService);
//        Assert.assertEquals("The constructor service method result was wrong","org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl1",constructorService.whoAmI());
        PropertyService constructorFilteredService = provider.getConstructorFilteredService();
        Assert.assertNotNull("The constructor filtered service was null", constructorFilteredService);
        Assert.assertEquals("The constructor filtered service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", constructorFilteredService.whoAmI());
        PropertyService constructorQualifiedService = provider.getConstructorQualifiedService();
        Assert.assertNotNull("The constructor qualified service was null", constructorQualifiedService);
        Assert.assertEquals("The constructor qualified service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", constructorQualifiedService.whoAmI());
        PropertyService constructorFilteredFromQualifierService = provider.getConstructorFilteredFromQualifierService();
        Assert.assertNotNull("The constructor filtered from qualifier service was null", constructorFilteredFromQualifierService);
        Assert.assertEquals("The constructor filtered from qualifier service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", constructorFilteredFromQualifierService.whoAmI());
        PropertyService constructorQualifiedFromPropertyService = provider.getConstructorQualifiedFromPropertyService();
        Assert.assertNotNull("The constructor qualified from property service was null", constructorQualifiedFromPropertyService);
        Assert.assertEquals("The constructor qualified from property service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", constructorQualifiedFromPropertyService.whoAmI());
        PropertyService constructorOtherFilteredService = provider.getConstructorOtherFilteredService();
        Assert.assertNotNull("The constructor other filtered service was null", constructorOtherFilteredService);
        Assert.assertEquals("The constructor other filtered service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", constructorOtherFilteredService.whoAmI());

//        PropertyService initializerService = provider.getInitializerService();
//        Assert.assertNotNull("The initializer service was null", initializerService);
//        Assert.assertEquals("The initializer service method result was wrong","org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl1",initializerService.whoAmI());
        PropertyService initializerFilteredService = provider.getInitializerFilteredService();
        Assert.assertNotNull("The initializer filtered service was null", initializerFilteredService);
        Assert.assertEquals("The initializer filtered service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", initializerFilteredService.whoAmI());
        PropertyService initializerQualifiedService = provider.getInitializerQualifiedService();
        Assert.assertNotNull("The initializer qualified service was null", initializerQualifiedService);
        Assert.assertEquals("The initializer qualified service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", initializerQualifiedService.whoAmI());
        PropertyService initializerFilteredFromQualifierService = provider.getInitializerFilteredFromQualifierService();
        Assert.assertNotNull("The initializer filtered from qualifier service was null", initializerFilteredFromQualifierService);
        Assert.assertEquals("The initializer filtered from qualifier service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", initializerFilteredFromQualifierService.whoAmI());
        PropertyService initializerQualifiedFromPropertyService = provider.getInitializerQualifiedFromPropertyService();
        Assert.assertNotNull("The initializer qualified from property service was null", initializerQualifiedFromPropertyService);
        Assert.assertEquals("The initializer qualified from property service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", initializerQualifiedFromPropertyService.whoAmI());
        PropertyService initializerOtherFilteredService = provider.getInitializerOtherFilteredService();
        Assert.assertNotNull("The initializer other filtered service was null", initializerOtherFilteredService);
        Assert.assertEquals("The initializer other filtered service method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", initializerOtherFilteredService.whoAmI());

        Service<PropertyService> serviceProvider = provider.getServiceProvider();
        Assert.assertNotNull("The service provider was null", serviceProvider);
        Assert.assertFalse("The service provider was unsatisfied", serviceProvider.isUnsatisfied());
        Assert.assertTrue("The service provider was not ambiguous", serviceProvider.isAmbiguous());
        PropertyService serviceProviderGet = serviceProvider.get();
        Assert.assertNotNull("The service provider instance was null", serviceProviderGet);
        Assert.assertEquals("The service provider method result was wrong", String.class, serviceProviderGet.whoAmI().getClass());
        Service<PropertyService> filteredServiceProvider = provider.getFilteredServiceProvider();
        Assert.assertNotNull("The filtered service provider was null", filteredServiceProvider);
        Assert.assertFalse("The filtered service provider was unsatisfied or ambiguous", filteredServiceProvider.isUnsatisfied() || filteredServiceProvider.isAmbiguous());
        PropertyService filteredServiceProviderGet = filteredServiceProvider.get();
        Assert.assertNotNull("The filtered service provider instance was null", filteredServiceProviderGet);
        Assert.assertEquals("The filtered service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", filteredServiceProviderGet.whoAmI());
        Service<PropertyService> qualifiedServiceProvider = provider.getQualifiedServiceProvider();
        Assert.assertNotNull("The qualified service provider was null", qualifiedServiceProvider);
        Assert.assertFalse("The qualified service provider was unsatisfied or ambiguous", qualifiedServiceProvider.isUnsatisfied() || qualifiedServiceProvider.isAmbiguous());
        PropertyService qualifiedServiceProviderGet = qualifiedServiceProvider.get();
        Assert.assertNotNull("The qualified service provider instance was null", qualifiedServiceProviderGet);
        Assert.assertEquals("The qualified service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", qualifiedServiceProviderGet.whoAmI());
        Service<PropertyService> filteredFromQualifierServiceProvider = provider.getFilteredFromQualifierServiceProvider();
        Assert.assertNotNull("The filtered from qualifier service provider was null", filteredFromQualifierServiceProvider);
        Assert.assertFalse("The filtered from qualifier service provider was unsatisfied or ambiguous", filteredFromQualifierServiceProvider.isUnsatisfied() || filteredFromQualifierServiceProvider.isAmbiguous());
        PropertyService filteredFromQualifierServiceProviderGet = filteredFromQualifierServiceProvider.get();
        Assert.assertNotNull("The filtered from qualifier service provider instance was null", filteredFromQualifierServiceProviderGet);
        Assert.assertEquals("The filtered from qualifier service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", filteredFromQualifierServiceProviderGet.whoAmI());
        Service<PropertyService> qualifiedFromPropertyServiceProvider = provider.getQualifiedFromPropertyServiceProvider();
        Assert.assertNotNull("The qualified from property service provider was null", qualifiedFromPropertyServiceProvider);
        Assert.assertFalse("The qualified from property service provider was unsatisfied or ambiguous", qualifiedFromPropertyServiceProvider.isUnsatisfied() || qualifiedFromPropertyServiceProvider.isAmbiguous());
        PropertyService qualifiedFromPropertyServiceProviderGet = qualifiedFromPropertyServiceProvider.get();
        Assert.assertNotNull("The qualified from property service provider instance was null", qualifiedFromPropertyServiceProviderGet);
        Assert.assertEquals("The qualified from property service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", qualifiedFromPropertyServiceProviderGet.whoAmI());
        Service<PropertyService> otherFilteredServiceProvider = provider.getOtherFilteredServiceProvider();
        Assert.assertNotNull("The other filtered service provider was null", otherFilteredServiceProvider);
        Assert.assertFalse("The other filtered service provider was unsatisfied or ambiguous", otherFilteredServiceProvider.isUnsatisfied() || otherFilteredServiceProvider.isAmbiguous());
        PropertyService otherFilteredServiceProviderGet = otherFilteredServiceProvider.get();
        Assert.assertNotNull("The other filtered service provider instance was null", otherFilteredServiceProviderGet);
        Assert.assertEquals("The other filtered service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", otherFilteredServiceProviderGet.whoAmI());

        Service<PropertyService> constructorServiceProvider = provider.getConstructorServiceProvider();
        Assert.assertNotNull("The constructor service provider was null", constructorServiceProvider);
        Assert.assertFalse("The constructor service provider was unsatisfied", constructorServiceProvider.isUnsatisfied());
        Assert.assertTrue("The constructor service provider was not ambiguous", constructorServiceProvider.isAmbiguous());
        PropertyService constructorServiceProviderGet = constructorServiceProvider.get();
        Assert.assertNotNull("The constructor service provider instance was null", constructorServiceProviderGet);
        Assert.assertEquals("The constructor service provider method result was wrong", String.class, constructorServiceProviderGet.whoAmI().getClass());
        Service<PropertyService> constructorFilteredServiceProvider = provider.getConstructorFilteredServiceProvider();
        Assert.assertNotNull("The constructor filtered service provider was null", constructorFilteredServiceProvider);
        Assert.assertFalse("The constructor filtered service provider was unsatisfied or ambiguous", constructorFilteredServiceProvider.isUnsatisfied() || constructorFilteredServiceProvider.isAmbiguous());
        PropertyService constructorFilteredServiceProviderGet = constructorFilteredServiceProvider.get();
        Assert.assertNotNull("The constructor filtered service provider instance was null", constructorFilteredServiceProviderGet);
        Assert.assertEquals("The constructor filtered service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", constructorFilteredServiceProviderGet.whoAmI());
        Service<PropertyService> constructorQualifiedServiceProvider = provider.getConstructorQualifiedServiceProvider();
        Assert.assertNotNull("The constructor qualified service provider was null", constructorQualifiedServiceProvider);
        Assert.assertFalse("The constructor qualified service provider was unsatisfied or ambiguous", constructorQualifiedServiceProvider.isUnsatisfied() || constructorQualifiedServiceProvider.isAmbiguous());
        PropertyService constructorQualifiedServiceProviderGet = constructorQualifiedServiceProvider.get();
        Assert.assertNotNull("The constructor qualified service provider instance was null", constructorQualifiedServiceProviderGet);
        Assert.assertEquals("The constructor qualified service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", constructorQualifiedServiceProviderGet.whoAmI());
        Service<PropertyService> constructorFilteredFromQualifierServiceProvider = provider.getConstructorFilteredFromQualifierServiceProvider();
        Assert.assertNotNull("The constructor filtered from qualifier service provider was null", constructorFilteredFromQualifierServiceProvider);
        Assert.assertFalse("The constructor filtered from qualifier service provider was unsatisfied or ambiguous", constructorFilteredFromQualifierServiceProvider.isUnsatisfied() || constructorFilteredFromQualifierServiceProvider.isAmbiguous());
        PropertyService constructorFilteredFromQualifierServiceProviderGet = constructorFilteredFromQualifierServiceProvider.get();
        Assert.assertNotNull("The constructor filtered from qualifier service provider instance was null", constructorFilteredFromQualifierServiceProviderGet);
        Assert.assertEquals("The constructor filtered from qualifier service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", constructorFilteredFromQualifierServiceProviderGet.whoAmI());
        Service<PropertyService> constructorQualifiedFromPropertyServiceProvider = provider.getConstructorQualifiedFromPropertyServiceProvider();
        Assert.assertNotNull("The constructor qualified from property service provider was null", constructorQualifiedFromPropertyServiceProvider);
        Assert.assertFalse("The constructor qualified from property service provider was unsatisfied or ambiguous", constructorQualifiedFromPropertyServiceProvider.isUnsatisfied() || constructorQualifiedFromPropertyServiceProvider.isAmbiguous());
        PropertyService constructorQualifiedFromPropertyServiceProviderGet = constructorQualifiedFromPropertyServiceProvider.get();
        Assert.assertNotNull("The constructor qualified from property service provider instance was null", constructorQualifiedFromPropertyServiceProviderGet);
        Assert.assertEquals("The constructor qualified from property service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", constructorQualifiedFromPropertyServiceProviderGet.whoAmI());
        Service<PropertyService> constructorOtherFilteredServiceProvider = provider.getConstructorOtherFilteredServiceProvider();
        Assert.assertNotNull("The constructor other filtered service provider was null", constructorOtherFilteredServiceProvider);
        Assert.assertFalse("The constructor other filtered service provider was unsatisfied or ambiguous", constructorOtherFilteredServiceProvider.isUnsatisfied() || constructorOtherFilteredServiceProvider.isAmbiguous());
        PropertyService constructorOtherFilteredServiceProviderGet = constructorOtherFilteredServiceProvider.get();
        Assert.assertNotNull("The constructor other filtered service provider instance was null", constructorOtherFilteredServiceProviderGet);
        Assert.assertEquals("The constructor other filtered service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", constructorOtherFilteredServiceProviderGet.whoAmI());

        Service<PropertyService> initializerServiceProvider = provider.getInitializerServiceProvider();
        Assert.assertNotNull("The initializer service provider was null", initializerServiceProvider);
        Assert.assertFalse("The initializer service provider was unsatisfied", initializerServiceProvider.isUnsatisfied());
        Assert.assertTrue("The initializer service provider was not ambiguous", initializerServiceProvider.isAmbiguous());
        PropertyService initializerServiceProviderGet = initializerServiceProvider.get();
        Assert.assertNotNull("The initializer service provider instance was null", initializerServiceProviderGet);
        Assert.assertEquals("The initializer service provider method result was wrong", String.class, initializerServiceProviderGet.whoAmI().getClass());
        Service<PropertyService> initializerFilteredServiceProvider = provider.getInitializerFilteredServiceProvider();
        Assert.assertNotNull("The initializer filtered service provider was null", initializerFilteredServiceProvider);
        Assert.assertFalse("The initializer filtered service provider was unsatisfied or ambiguous", initializerFilteredServiceProvider.isUnsatisfied() || initializerFilteredServiceProvider.isAmbiguous());
        PropertyService initializerFilteredServiceProviderGet = initializerFilteredServiceProvider.get();
        Assert.assertNotNull("The initializer filtered service provider instance was null", initializerFilteredServiceProviderGet);
        Assert.assertEquals("The initializer filtered service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", initializerFilteredServiceProviderGet.whoAmI());
        Service<PropertyService> initializerQualifiedServiceProvider = provider.getInitializerQualifiedServiceProvider();
        Assert.assertNotNull("The initializer qualified service provider was null", initializerQualifiedServiceProvider);
        Assert.assertFalse("The initializer qualified service provider was unsatisfied or ambiguous", initializerQualifiedServiceProvider.isUnsatisfied() || initializerQualifiedServiceProvider.isAmbiguous());
        PropertyService initializerQualifiedServiceProviderGet = initializerQualifiedServiceProvider.get();
        Assert.assertNotNull("The initializer qualified service provider instance was null", initializerQualifiedServiceProviderGet);
        Assert.assertEquals("The initializer qualified service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", initializerQualifiedServiceProviderGet.whoAmI());
        Service<PropertyService> initializerFilteredFromQualifierServiceProvider = provider.getInitializerFilteredFromQualifierServiceProvider();
        Assert.assertNotNull("The initializer filtered from qualifier service provider was null", initializerFilteredFromQualifierServiceProvider);
        Assert.assertFalse("The initializer filtered from qualifier service provider was unsatisfied or ambiguous", initializerFilteredFromQualifierServiceProvider.isUnsatisfied() || initializerFilteredFromQualifierServiceProvider.isAmbiguous());
        PropertyService initializerFilteredFromQualifierServiceProviderGet = initializerFilteredFromQualifierServiceProvider.get();
        Assert.assertNotNull("The initializer filtered from qualifier service provider instance was null", initializerFilteredFromQualifierServiceProviderGet);
        Assert.assertEquals("The initializer filtered from qualifier service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl3", initializerFilteredFromQualifierServiceProviderGet.whoAmI());
        Service<PropertyService> initializerQualifiedFromPropertyServiceProvider = provider.getInitializerQualifiedFromPropertyServiceProvider();
        Assert.assertNotNull("The initializer qualified from property service provider was null", initializerQualifiedFromPropertyServiceProvider);
        Assert.assertFalse("The initializer qualified from property service provider was unsatisfied or ambiguous", initializerQualifiedFromPropertyServiceProvider.isUnsatisfied() || initializerQualifiedFromPropertyServiceProvider.isAmbiguous());
        PropertyService initializerQualifiedFromPropertyServiceProviderGet = initializerQualifiedFromPropertyServiceProvider.get();
        Assert.assertNotNull("The initializer qualified from property service provider instance was null", initializerQualifiedFromPropertyServiceProviderGet);
        Assert.assertEquals("The initializer qualified from property service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", initializerQualifiedFromPropertyServiceProviderGet.whoAmI());
        Service<PropertyService> initializerOtherFilteredServiceProvider = provider.getInitializerOtherFilteredServiceProvider();
        Assert.assertNotNull("The initializer other filtered service provider was null", initializerOtherFilteredServiceProvider);
        Assert.assertFalse("The initializer other filtered service provider was unsatisfied or ambiguous", initializerOtherFilteredServiceProvider.isUnsatisfied() || initializerOtherFilteredServiceProvider.isAmbiguous());
        PropertyService initializerOtherFilteredServiceProviderGet = initializerOtherFilteredServiceProvider.get();
        Assert.assertNotNull("The initializer other filtered service provider instance was null", initializerOtherFilteredServiceProviderGet);
        Assert.assertEquals("The initializer other filtered service provider method result was wrong", "org.jboss.weld.osgi.tests.bundle1.impl.PropertyServiceImpl2", initializerOtherFilteredServiceProviderGet.whoAmI());
    }

    @Test
    //@Ignore
    public void hashCodeCallTest(BundleContext context) throws InterruptedException, InvalidSyntaxException {
        Environment.waitForEnvironment(context);

        ServiceReference[] serviceProviderReferences = context.getServiceReferences(ServiceProvider.class.getName(), null);
        Assert.assertNotNull("The service provider reference array was null", serviceProviderReferences);
        Assert.assertEquals("The number of service provider implementations was wrong", 1, serviceProviderReferences.length);
        ServiceProvider provider = (ServiceProvider) context.getService(serviceProviderReferences[0]);
        Assert.assertNotNull("The service provider was null", provider);


        PersonalizedHashCodeService hashCodeService = provider.getPersonalizedHashCodeService();
        Assert.assertNotNull("The hashCode service was null", hashCodeService);
        Assert.assertEquals("The hashCode service method result was wrong", 42, hashCodeService.hashCode());
    }
}
