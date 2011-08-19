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

package org.jboss.weld.osgi.tests.bundle1.util;

import org.jboss.weld.osgi.tests.bundle1.api.Name;
import org.jboss.weld.osgi.tests.bundle1.api.PersonalizedHashCodeService;
import org.jboss.weld.osgi.tests.bundle1.api.PropertyService;

import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.OSGiService;
import org.jboss.weld.environment.osgi.api.annotation.Publish;

@Publish
public class ServiceProvider {

    @Inject
    public ServiceProvider(@OSGiService PropertyService constructorService,
                           @OSGiService @Filter("(Name.value=1)") PropertyService constructorFilteredService,
                           @OSGiService @Name("2") PropertyService constructorQualifiedService,
                           @OSGiService @Name("1") PropertyService constructorQualifiedFromPropertyService,
                           @OSGiService @Filter("(Name.value=2)") PropertyService constructorFilteredFromQualifierService,
                           @OSGiService @Filter("(Name.value=1)") PropertyService constructorOtherFilteredService,
                           Service<PropertyService> constructorServiceProvider,
                           @Filter("(Name.value=1)") Service<PropertyService> constructorFilteredServiceProvider,
                           @Name("2") Service<PropertyService> constructorQualifiedServiceProvider,
                           @Name("1") Service<PropertyService> constructorQualifiedFromPropertyServiceProvider,
                           @Filter("(Name.value=2)") Service<PropertyService> constructorFilteredFromQualifierServiceProvider,
                           @Filter("(Name.value=1)") Service<PropertyService> constructorOtherFilteredServiceProvider) {
        this.constructorService = constructorService;
        this.constructorFilteredService = constructorFilteredService;
        this.constructorQualifiedService = constructorQualifiedService;
        this.constructorQualifiedFromPropertyService = constructorQualifiedFromPropertyService;
        this.constructorFilteredFromQualifierService = constructorFilteredFromQualifierService;
        this.constructorOtherFilteredService = constructorOtherFilteredService;
        this.constructorServiceProvider = constructorServiceProvider;
        this.constructorFilteredServiceProvider = constructorFilteredServiceProvider;
        this.constructorQualifiedServiceProvider = constructorQualifiedServiceProvider;
        this.constructorQualifiedFromPropertyServiceProvider = constructorQualifiedFromPropertyServiceProvider;
        this.constructorFilteredFromQualifierServiceProvider = constructorFilteredFromQualifierServiceProvider;
        this.constructorOtherFilteredServiceProvider = constructorOtherFilteredServiceProvider;
    }

    private PropertyService constructorService;
    private PropertyService constructorFilteredService;
    private PropertyService constructorQualifiedService;
    private PropertyService constructorQualifiedFromPropertyService;
    private PropertyService constructorFilteredFromQualifierService;
    private PropertyService constructorOtherFilteredService;

    private Service<PropertyService> constructorServiceProvider;
    private Service<PropertyService> constructorFilteredServiceProvider;
    private Service<PropertyService> constructorQualifiedServiceProvider;
    private Service<PropertyService> constructorQualifiedFromPropertyServiceProvider;
    private Service<PropertyService> constructorFilteredFromQualifierServiceProvider;
    private Service<PropertyService> constructorOtherFilteredServiceProvider;

    private PropertyService initializerService;
    private PropertyService initializerFilteredService;
    private PropertyService initializerQualifiedService;
    private PropertyService initializerQualifiedFromPropertyService;
    private PropertyService initializerFilteredFromQualifierService;
    private PropertyService initializerOtherFilteredService;

    private Service<PropertyService> initializerServiceProvider;
    private Service<PropertyService> initializerFilteredServiceProvider;
    private Service<PropertyService> initializerQualifiedServiceProvider;
    private Service<PropertyService> initializerQualifiedFromPropertyServiceProvider;
    private Service<PropertyService> initializerFilteredFromQualifierServiceProvider;
    private Service<PropertyService> initializerOtherFilteredServiceProvider;

    @Inject
    public void setInitializerService(@OSGiService PropertyService initializerService) {
        this.initializerService = initializerService;
    }

    @Inject
    public void setInitializerFilteredService(@OSGiService @Filter("(Name.value=1)") PropertyService initializerFilteredService) {
        this.initializerFilteredService = initializerFilteredService;
    }

    @Inject
    public void setInitializerQualifiedService(@OSGiService @Name("2") PropertyService initializerQualifiedService) {
        this.initializerQualifiedService = initializerQualifiedService;
    }

    @Inject
    public void setInitializerQualifiedFromPropertyService(@OSGiService @Name("1") PropertyService initializerQualifiedFromPropertyService) {
        this.initializerQualifiedFromPropertyService = initializerQualifiedFromPropertyService;
    }

    @Inject
    public void setInitializerFilteredFromQualifierService(@OSGiService @Filter("(Name.value=2)") PropertyService initializerFilteredFromQualifierService) {
        this.initializerFilteredFromQualifierService = initializerFilteredFromQualifierService;
    }

    @Inject
    public void setInitializerOtherFilteredService(@OSGiService @Filter("(Name.value=1)") PropertyService initializerOtherFilteredService) {
        this.initializerOtherFilteredService = initializerOtherFilteredService;
    }

    @Inject
    public void setInitializerServiceProvider(Service<PropertyService> initializerServiceProvider) {
        this.initializerServiceProvider = initializerServiceProvider;
    }

    @Inject
    public void setInitializerFilteredServiceProvider(@Filter("(Name.value=1)") Service<PropertyService> initializerFilteredServiceProvider) {
        this.initializerFilteredServiceProvider = initializerFilteredServiceProvider;
    }

    @Inject
    public void setInitializerQualifiedServiceProvider(@Name("2") Service<PropertyService> initializerQualifiedServiceProvider) {
        this.initializerQualifiedServiceProvider = initializerQualifiedServiceProvider;
    }

    @Inject
    public void setInitializerQualifiedFromPropertyServiceProvider(@Name("1") Service<PropertyService> initializerQualifiedFromPropertyServiceProvider) {
        this.initializerQualifiedFromPropertyServiceProvider = initializerQualifiedFromPropertyServiceProvider;
    }

    @Inject
    public void setInitializerFilteredFromQualifierServiceProvider(@Filter("(Name.value=2)") Service<PropertyService> initializerFilteredFromQualifierServiceProvider) {
        this.initializerFilteredFromQualifierServiceProvider = initializerFilteredFromQualifierServiceProvider;
    }

    @Inject
    public void setInitializerOtherFilteredServiceProvider(@Filter("(Name.value=1)") Service<PropertyService> initializerOtherFilteredServiceProvider) {
        this.initializerOtherFilteredServiceProvider = initializerOtherFilteredServiceProvider;
    }

    @Inject
    @OSGiService
    private PropertyService service;

    @Inject
    @OSGiService
    @Filter("(Name.value=1)")
    private PropertyService filteredService;

    @Inject
    @OSGiService
    @Name("2")
    private PropertyService qualifiedService;

    @Inject
    @OSGiService
    @Name("1")
    private PropertyService qualifiedFromPropertyService;

    @Inject
    @OSGiService
    @Filter("(Name.value=2)")
    private PropertyService filteredFromQualifierService;

    @Inject
    @OSGiService
    @Filter("(Name.value=1)")
    private PropertyService otherFilteredService;

    @Inject
    private Service<PropertyService> serviceProvider;

    @Inject
    @Filter("(Name.value=1)")
    private Service<PropertyService> filteredServiceProvider;

    @Inject
    @Name("2")
    private Service<PropertyService> qualifiedServiceProvider;

    @Inject
    @Name("1")
    private Service<PropertyService> qualifiedFromPropertyServiceProvider;

    @Inject
    @Filter("(Name.value=2)")
    private Service<PropertyService> filteredFromQualifierServiceProvider;

    @Inject
    @Filter("(Name.value=1)")
    private Service<PropertyService> otherFilteredServiceProvider;

    public PropertyService getConstructorService() {
        return constructorService;
    }

    public PropertyService getConstructorFilteredService() {
        return constructorFilteredService;
    }

    public PropertyService getConstructorQualifiedService() {
        return constructorQualifiedService;
    }

    public PropertyService getConstructorQualifiedFromPropertyService() {
        return constructorQualifiedFromPropertyService;
    }

    public PropertyService getConstructorFilteredFromQualifierService() {
        return constructorFilteredFromQualifierService;
    }

    public PropertyService getConstructorOtherFilteredService() {
        return constructorOtherFilteredService;
    }

    public Service<PropertyService> getConstructorServiceProvider() {
        return constructorServiceProvider;
    }

    public Service<PropertyService> getConstructorFilteredServiceProvider() {
        return constructorFilteredServiceProvider;
    }

    public Service<PropertyService> getConstructorQualifiedServiceProvider() {
        return constructorQualifiedServiceProvider;
    }

    public Service<PropertyService> getConstructorQualifiedFromPropertyServiceProvider() {
        return constructorQualifiedFromPropertyServiceProvider;
    }

    public Service<PropertyService> getConstructorFilteredFromQualifierServiceProvider() {
        return constructorFilteredFromQualifierServiceProvider;
    }

    public Service<PropertyService> getConstructorOtherFilteredServiceProvider() {
        return constructorOtherFilteredServiceProvider;
    }

    public PropertyService getInitializerService() {
        return initializerService;
    }

    public PropertyService getInitializerFilteredService() {
        return initializerFilteredService;
    }

    public PropertyService getInitializerQualifiedService() {
        return initializerQualifiedService;
    }

    public PropertyService getInitializerQualifiedFromPropertyService() {
        return initializerQualifiedFromPropertyService;
    }

    public PropertyService getInitializerFilteredFromQualifierService() {
        return initializerFilteredFromQualifierService;
    }

    public PropertyService getInitializerOtherFilteredService() {
        return initializerOtherFilteredService;
    }

    public Service<PropertyService> getInitializerServiceProvider() {
        return initializerServiceProvider;
    }

    public Service<PropertyService> getInitializerFilteredServiceProvider() {
        return initializerFilteredServiceProvider;
    }

    public Service<PropertyService> getInitializerQualifiedServiceProvider() {
        return initializerQualifiedServiceProvider;
    }

    public Service<PropertyService> getInitializerQualifiedFromPropertyServiceProvider() {
        return initializerQualifiedFromPropertyServiceProvider;
    }

    public Service<PropertyService> getInitializerFilteredFromQualifierServiceProvider() {
        return initializerFilteredFromQualifierServiceProvider;
    }

    public Service<PropertyService> getInitializerOtherFilteredServiceProvider() {
        return initializerOtherFilteredServiceProvider;
    }

    public PropertyService getService() {
        return service;
    }

    public PropertyService getFilteredService() {
        return filteredService;
    }

    public PropertyService getQualifiedService() {
        return qualifiedService;
    }

    public PropertyService getQualifiedFromPropertyService() {
        return qualifiedFromPropertyService;
    }

    public PropertyService getFilteredFromQualifierService() {
        return filteredFromQualifierService;
    }

    public PropertyService getOtherFilteredService() {
        return otherFilteredService;
    }

    public Service<PropertyService> getServiceProvider() {
        return serviceProvider;
    }

    public Service<PropertyService> getFilteredServiceProvider() {
        return filteredServiceProvider;
    }

    public Service<PropertyService> getQualifiedServiceProvider() {
        return qualifiedServiceProvider;
    }

    public Service<PropertyService> getQualifiedFromPropertyServiceProvider() {
        return qualifiedFromPropertyServiceProvider;
    }

    public Service<PropertyService> getFilteredFromQualifierServiceProvider() {
        return filteredFromQualifierServiceProvider;
    }

    public Service<PropertyService> getOtherFilteredServiceProvider() {
        return otherFilteredServiceProvider;
    }

    @Inject
    @OSGiService
    private PersonalizedHashCodeService personalizedHashCodeService;

    public PersonalizedHashCodeService getPersonalizedHashCodeService() {
        return personalizedHashCodeService;
    }
}
