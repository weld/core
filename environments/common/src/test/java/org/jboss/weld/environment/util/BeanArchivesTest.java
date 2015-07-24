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
 */
package org.jboss.weld.environment.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 *
 * @author Martin Kouba
 */
public class BeanArchivesTest {

    @Test
    public void testFindBeanClassesDeployedInMultipleBeanArchives() {

        String beanClass = "com.foo.Bar";
        BeanDeploymentArchive bda1 = new WeldBeanDeploymentArchive("foo", ImmutableList.of(beanClass), null);
        BeanDeploymentArchive bda2 = new WeldBeanDeploymentArchive("bar", ImmutableList.of(beanClass), null);

        Multimap<String, BeanDeploymentArchive> problems = BeanArchives.findBeanClassesDeployedInMultipleBeanArchives(Collections.singleton(bda1));
        assertTrue(problems.isEmpty());

        problems = BeanArchives.findBeanClassesDeployedInMultipleBeanArchives(ImmutableSet.of(bda1, bda2));
        assertFalse(problems.isEmpty());
        assertEquals(1, problems.keySet().size());
        Entry<String, Collection<BeanDeploymentArchive>> entry = problems.asMap().entrySet().iterator().next();
        assertEquals(beanClass, entry.getKey());
        assertEquals(2, entry.getValue().size());
        for (BeanDeploymentArchive bda : entry.getValue()) {
            if (!bda.getId().equals("foo") && !bda.getId().equals("bar")) {
                fail();
            }
        }
    }

}
