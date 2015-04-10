/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
 */package org.jboss.weld.environment.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

/**
 *
 * @author Martin Kouba
 */
public final class BeanArchives {

    private BeanArchives() {
    }

    /**
     *
     * @param beanArchives
     * @return a {@link Multimap} that maps problematic bean class to the set of bean archives where it is deployed
     */
    public static <B extends BeanDeploymentArchive> Multimap<String, BeanDeploymentArchive> findBeanClassesDeployedInMultipleBeanArchives(Set<B> beanArchives) {

        if (beanArchives.size() == 1) {
            return ImmutableSetMultimap.of();
        }

        SetMultimap<String, BeanDeploymentArchive> problems = HashMultimap.create();

        if (beanArchives.size() == 2) {
            // Find the set that contains all bean classes of the first bean archive that also belong to the second bean archive
            Iterator<B> iterator = beanArchives.iterator();
            BeanDeploymentArchive first = iterator.next();
            BeanDeploymentArchive second = iterator.next();
            Set<String> intersection = new HashSet<String>(first.getBeanClasses());
            intersection.retainAll(second.getBeanClasses());
            if (!intersection.isEmpty()) {
                List<BeanDeploymentArchive> bdas = ImmutableList.of(first, second);
                for (String beanClass : intersection) {
                    problems.putAll(beanClass, bdas);
                }
            }
        } else if (beanArchives.size() > 2) {
            // First collect the data
            SetMultimap<String, BeanDeploymentArchive> beanClassOccurrences = HashMultimap.create();
            for (BeanDeploymentArchive beanArchive : beanArchives) {
                for (String beanClass : beanArchive.getBeanClasses()) {
                    beanClassOccurrences.put(beanClass, beanArchive);
                }
            }
            // Then identify problematic bean classes
            for (Entry<String, Collection<BeanDeploymentArchive>> entry : beanClassOccurrences.asMap().entrySet()) {
                if (entry.getValue().size() > 1) {
                    problems.putAll(entry.getKey(), entry.getValue());
                }
            }
        }
        return problems;
    }

}
