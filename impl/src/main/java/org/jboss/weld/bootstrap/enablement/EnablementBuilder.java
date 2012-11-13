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
package org.jboss.weld.bootstrap.enablement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.bootstrap.BeanDeployment;

public class EnablementBuilder {

    private final Set<BeanDeployment> deployments;

    private final EnablementBuilderFragment interceptors;
    private final EnablementBuilderFragment decorators;
    private final EnablementBuilderFragment alternatives;
    private boolean initialized;

    private final Map<BeanDeployment, ModuleEnablementBuilder> moduleEnablementBuilders;

    public EnablementBuilder() {
        this.deployments = new HashSet<BeanDeployment>();
        this.interceptors = new EnablementBuilderFragment();
        this.decorators = new EnablementBuilderFragment();
        this.alternatives = new EnablementBuilderFragment();
        this.moduleEnablementBuilders = new HashMap<BeanDeployment, ModuleEnablementBuilder>();
    }

    public void registerBeanDeployment(BeanDeployment deployment) {
        deployments.add(deployment);
    }

    protected void processDeployments() {
        for (BeanDeployment deployment : deployments) {
            interceptors.processGlobalRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledInterceptors());
            decorators.processGlobalRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledDecorators());
            alternatives.processGlobalRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledAlternatives());
        }
        for (BeanDeployment deployment : deployments) {
            interceptors.processLocalRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledInterceptors());
            decorators.processLocalRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledDecorators());
            alternatives.processLocalRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledAlternatives());

            interceptors.processLegacyRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledInterceptors());
            decorators.processLegacyRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledDecorators());
            alternatives.processLegacyRecords(deployment, deployment.getBeanDeploymentArchive().getBeansXml().getEnabledAlternatives());
        }
        for (BeanDeployment deployment : deployments) {
            moduleEnablementBuilders.put(deployment, new ModuleEnablementBuilder(interceptors.create(deployment), decorators.create(deployment), alternatives.create(deployment)));
        }
    }

    /**
     * Create {@link ModuleEnablementBuilder} for a given {@link BeanDeployment}.
     */
    public ModuleEnablementBuilder getModuleEnablementBuilder(BeanDeployment deployment) {
        if (!initialized) {
            processDeployments();
            initialized = true;
        }
        return moduleEnablementBuilders.get(deployment);
    }
}
