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
package org.jboss.weld.environment.osgi.impl.integration.discovery.bundle;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.osgi.impl.integration.Weld;
import org.jboss.weld.environment.osgi.impl.integration.discovery.AbstractWeldOSGiDeployment;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.osgi.framework.Bundle;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Weld Deployment for OSGi environment.
 * <p/>
 * It allows to create a complete
 * {@link org.jboss.weld.bootstrap.spi.Deployment} for CDI manageable OSGi
 * {@link Bundle}.
 *
 * @author Peter Royle
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class BundleDeployment extends AbstractWeldOSGiDeployment {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(Weld.class);

    private final BeanDeploymentArchive beanDeploymentArchive;

    public BundleDeployment(Bundle bundle,
                            Bootstrap bootstrap,
                            BundleBeanDeploymentArchiveFactory factory) {
        super(bootstrap, bundle);
        this.beanDeploymentArchive = factory.scan(bundle, bootstrap);
        if (beanDeploymentArchive != null) {
            ResourceLoader loader = new BundleResourceLoader(bundle);
            this.beanDeploymentArchive.getServices().
                    add(ResourceLoader.class, loader);
        }
    }

    @Override
    public List<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return Collections.singletonList(beanDeploymentArchive);
    }

    @Override
    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        return beanDeploymentArchive;
    }

    public BeanDeploymentArchive getBeanDeploymentArchive() {
        return beanDeploymentArchive;
    }

}
