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
package org.jboss.weld.ejb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ApiAbstraction;

/**
 * Utility class for EJB classes etc. EJB metadata should NOT be inspected here
 *
 * @author Pete Muir
 */
public class EJBApiAbstraction extends ApiAbstraction implements Service {

    public EJBApiAbstraction(ResourceLoader resourceLoader) {
        super(resourceLoader);
        EJB_ANNOTATION_CLASS = annotationTypeForName("javax.ejb.EJB");
        TIMEOUT_ANNOTATION_CLASS = annotationTypeForName("javax.ejb.Timeout");
        TRANSACTION_MANAGEMENT = annotationTypeForName("javax.ejb.TransactionManagement");
        final Class<?> TRANSACTION_MANAGEMENT_TYPE = classForName("javax.ejb.TransactionManagementType");
        if (TRANSACTION_MANAGEMENT_TYPE.equals(Dummy.class)) {
            CONTAINER_MANAGED_TRANSACTION_MANAGEMENT_ENUM_VALUE = DummyEnum.DUMMY_VALUE;
        } else {
            CONTAINER_MANAGED_TRANSACTION_MANAGEMENT_ENUM_VALUE = enumValue(TRANSACTION_MANAGEMENT_TYPE, "CONTAINER");
        }
    }

    public final Class<? extends Annotation> EJB_ANNOTATION_CLASS;
    public final Class<? extends Annotation> TIMEOUT_ANNOTATION_CLASS;
    private final Class<? extends Annotation> TRANSACTION_MANAGEMENT;
    private final Object CONTAINER_MANAGED_TRANSACTION_MANAGEMENT_ENUM_VALUE;

    public void cleanup() {
    }

    public boolean isSessionBeanWithContainerManagedTransactions(Bean<?> bean) {
        if (bean instanceof SessionBean<?>) {
            SessionBean<?> sessionBean = (SessionBean<?>) bean;
            Annotation transactionManagementAnnotation = sessionBean.getAnnotated().getAnnotation(TRANSACTION_MANAGEMENT);
            if (transactionManagementAnnotation == null || transactionManagementAnnotation instanceof DummyAnnotation) {
                return true;
            }
            Object value;
            try {
                Method method = transactionManagementAnnotation.annotationType().getMethod("value");
                value = method.invoke(transactionManagementAnnotation);
            } catch (NoSuchMethodException e) {
                return true;
            } catch (Exception e) {
                throw new WeldException(e);
            }
            return CONTAINER_MANAGED_TRANSACTION_MANAGEMENT_ENUM_VALUE.equals(value);
        }
        return false;
    }

}
