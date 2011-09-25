/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.test.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.inject.Inject;

/**
 * @author Peter Royle
 */
@ApplicationScoped
public class MainTestBean {

    boolean initialised = false;
    ParametersTestBean parametersTestBean;

    public MainTestBean() {
    }

    @Inject
    public MainTestBean(ParametersTestBean paramsTestBean) {
        this.initialised = true;
        this.parametersTestBean = paramsTestBean;
        // this call is important. It invokes initialiser on the proxy
        paramsTestBean.getParameters();
    }

    public void mainMethod(@Observes AfterDeploymentValidation after) {
        System.out.println("Starting main test app");
    }

    public ParametersTestBean getParametersTestBean() {
        return parametersTestBean;
    }

    public boolean isInitialised() {
        return initialised;
    }

}
