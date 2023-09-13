/*
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
package org.jboss.weld.environment.deployment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.resources.ManagerObjectFactory;
import org.jboss.weld.xml.BeansXmlParser;

/**
 *
 *
 * @author Pete Muir
 * @author Matej Briškár
 * @author Martin Kouba
 */
public class WeldBeanDeploymentArchive extends AbstractWeldBeanDeploymentArchive {

    private final Collection<String> beanClasses;
    private final Collection<Class<?>> loadedBeanClasses;
    private final Collection<String> knownClasses;

    private final BeansXml beansXml;

    private volatile Collection<BeanDeploymentArchive> accessibleBeanDeploymentArchives;

    /**
     *
     * @param id
     * @param beanClasses The collection should be mutable
     * @param beansXml
     * @param accessibleBeanDeploymentArchives
     */
    public WeldBeanDeploymentArchive(String id, Collection<String> beanClasses, BeansXml beansXml,
            Set<WeldBeanDeploymentArchive> beanDeploymentArchives) {
        this(id, beanClasses, null, beansXml, beanDeploymentArchives, Collections.emptySet());
    }

    /**
     *
     * @param id
     * @param beanClasses
     * @param knownClasses
     * @param beansXml
     * @param beanDeploymentArchives
     * @param loadedBeanClasses
     */
    public WeldBeanDeploymentArchive(String id, Collection<String> beanClasses, Collection<String> knownClasses,
            BeansXml beansXml, Set<WeldBeanDeploymentArchive> accessibleBeanDeploymentArchives,
            Collection<Class<?>> loadedBeanClasses) {
        super(id);
        this.beanClasses = beanClasses;
        this.knownClasses = knownClasses;
        this.beansXml = beansXml;
        this.loadedBeanClasses = loadedBeanClasses;
        setAccessibleBeanDeploymentArchives(accessibleBeanDeploymentArchives);
    }

    /**
     *
     * @param id
     * @param beanClasses
     * @param beansXml
     */
    public WeldBeanDeploymentArchive(String id, Collection<String> beanClasses, BeansXml beansXml) {
        this(id, beanClasses, beansXml, Collections.emptySet());
    }

    /**
     *
     * @param id
     * @param beanClasses
     * @param knownClasses
     * @param beansXml
     */
    public WeldBeanDeploymentArchive(String id, Collection<String> beanClasses, Collection<String> knownClasses,
            BeansXml beansXml) {
        this(id, beanClasses, knownClasses, beansXml, Collections.emptySet(), Collections.emptySet());
    }

    @Override
    public Collection<String> getBeanClasses() {
        return Collections.unmodifiableCollection(beanClasses);
    }

    @Override
    public Collection<Class<?>> getLoadedBeanClasses() {
        return Collections.unmodifiableCollection(loadedBeanClasses);
    }

    @Override
    public Collection<String> getKnownClasses() {
        return knownClasses != null ? knownClasses : super.getKnownClasses();
    }

    @Override
    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return accessibleBeanDeploymentArchives;
    }

    @Override
    public BeansXml getBeansXml() {
        return beansXml;
    }

    public synchronized void setAccessibleBeanDeploymentArchives(Set<WeldBeanDeploymentArchive> beanDeploymentArchives) {
        accessibleBeanDeploymentArchives = Collections.unmodifiableSet(new HashSet<>(beanDeploymentArchives));
    }

    /**
     *
     * @param className
     */
    void addBeanClass(String className) {
        this.beanClasses.add(className);
    }

    /**
     *
     * @param bootstrap
     * @param archives
     * @return the "flat" bean deployment archive
     */
    public static <T extends BeanDeploymentArchive> WeldBeanDeploymentArchive merge(CDI11Bootstrap bootstrap,
            Iterable<T> archives) {
        BeansXml mergedBeansXml = BeansXmlParser.mergeExisting(archives, true);
        Set<String> beanClasses = new HashSet<String>();
        for (BeanDeploymentArchive archive : archives) {
            beanClasses.addAll(archive.getBeanClasses());
        }
        return new WeldBeanDeploymentArchive(ManagerObjectFactory.FLAT_BEAN_DEPLOYMENT_ID, beanClasses, mergedBeansXml);
    }

    /**
     *
     * @return <code>true</code> if there are no bean classes in this bean deployment archive, <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return beanClasses.isEmpty() && loadedBeanClasses.isEmpty();
    }

}
