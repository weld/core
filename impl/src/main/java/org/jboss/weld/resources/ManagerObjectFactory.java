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
package org.jboss.weld.resources;

import java.io.File;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;

public class ManagerObjectFactory implements ObjectFactory {

    /**
     * The id of a bean deployment archive whose BeanManager should be used as a result of a JNDI "java:comp/env/BeanManager"
     * lookup.
     */
    public static final String FLAT_BEAN_DEPLOYMENT_ID = "flat";

    public static final String WEB_INF_CLASSES = "/WEB-INF/classes";

    public static final String WEB_INF_CLASSES_FILE_PATH = File.separatorChar + "WEB-INF" + File.separatorChar + "classes";

    private final String contextId;

    public ManagerObjectFactory() {
        this(RegistrySingletonProvider.STATIC_INSTANCE);
    }

    public ManagerObjectFactory(String contextId) {
        this.contextId = contextId;
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        if (Container.available(contextId)) {
            for (Entry<BeanDeploymentArchive, BeanManagerImpl> entry : Container.instance(contextId).beanDeploymentArchives()
                    .entrySet()) {
                BeanDeploymentArchive bda = entry.getKey();
                if (bda.getId().equals(FLAT_BEAN_DEPLOYMENT_ID) || bda.getId().contains(WEB_INF_CLASSES_FILE_PATH)
                        || bda.getId().contains(WEB_INF_CLASSES)) {
                    return entry.getValue();
                }
            }
            throw BeanManagerLogger.LOG.cannotLocateBeanManager();
        } else {
            throw BeanManagerLogger.LOG.cannotLocateBeanManager();
        }
    }

}
