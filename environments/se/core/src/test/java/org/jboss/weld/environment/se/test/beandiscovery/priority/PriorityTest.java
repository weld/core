/*
 * JBoss, Home of Professional Open Source Copyright 2014, Red Hat, Inc. and/or
 * its affiliates, and individual contributors by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.weld.environment.se.test.beandiscovery.priority;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.enterprise.inject.Instance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.se.test.arquillian.WeldSEClassPath;
import org.jboss.weld.environment.se.test.isolation.ArchiveIsolationOverrideTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that @Priority annotation works for decorators, interceptors and
 * alternatives in archive isolation mode.
 */
@RunWith(Arquillian.class)
public class PriorityTest extends ArchiveIsolationOverrideTestBase {

    @Override
    public boolean isArchiveIsolationEnabled() {
        return true;
    }

    @Deployment(managed = false)
    public static Archive<?> getDeployment() {
        WeldSEClassPath archives = ShrinkWrap.create(WeldSEClassPath.class);

        JavaArchive archive01 = ShrinkWrap.create(BeanArchive.class)
                .addClasses(WhiteNoiseGenerator.class, SoundSource.class, Normalized.class);

        JavaArchive archive02 = ShrinkWrap.create(BeanArchive.class).annotated()
                .addClasses(EqualizingDecorator.class, NormalizingInterceptor.class, SineWaveGenerator.class);

        archives.add(archive01);
        archives.add(archive02);
        return archives;
    }

    @Test
    public void test(Instance<SoundSource> srcInstance) {
        NormalizingInterceptor.reset();
        EqualizingDecorator.reset();
        assertFalse(srcInstance.isAmbiguous());
        assertFalse(srcInstance.isUnsatisfied());
        SineWaveGenerator source = srcInstance.select(SineWaveGenerator.class).get();
        source.generateSound();
        assertEquals(1, NormalizingInterceptor.invocations);
        assertEquals(1, EqualizingDecorator.invocations);
    }

}
