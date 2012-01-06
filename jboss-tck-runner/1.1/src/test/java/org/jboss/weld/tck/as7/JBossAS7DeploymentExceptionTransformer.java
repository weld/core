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
package org.jboss.weld.tck.as7;

import java.util.List;

import javax.enterprise.inject.spi.DefinitionException;
import javax.enterprise.inject.spi.DeploymentException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.as.arquillian.container.ExceptionTransformer;

/**
 * Temporary replacement for NOOP {@link ExceptionTransformer} used by JBoss AS7 arquillian container.
 *
 * @see JBossAS7DeploymentExceptionTransformerExtension
 * @author Martin Kouba
 */
public class JBossAS7DeploymentExceptionTransformer implements DeploymentExceptionTransformer {

    private static final String DEPLOYMENT_EXCEPTION_FRAGMENT = "org.jboss.weld.exceptions.DeploymentException";

    private static final String DEFINITION_EXCEPTION_FRAGMENT = "org.jboss.weld.exceptions.DefinitionException";

    public Throwable transform(Throwable throwable) {

        // Arquillian sometimes returns InvocationException with nested AS7
        // exception and sometimes AS7 exception itself
        @SuppressWarnings("unchecked")
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(throwable);
        if (throwableList.size() < 1)
            return throwable;

        Throwable root = null;

        if (throwableList.size() == 1) {
            root = throwable;
        } else {
            root = ExceptionUtils.getRootCause(throwable);
        }

        if (root instanceof DeploymentException || root instanceof org.jboss.weld.exceptions.DeploymentException
                || root.getMessage().contains(DEPLOYMENT_EXCEPTION_FRAGMENT)) {
            return new DeploymentException(root);
        }
        if (root instanceof DefinitionException || root instanceof org.jboss.weld.exceptions.DefinitionException
                || root.getMessage().contains(DEFINITION_EXCEPTION_FRAGMENT)) {
            return new DefinitionException(root);
        }
        return throwable;
    }

}
