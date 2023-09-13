/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.discovery.disableJandex;

import java.io.File;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.weld.environment.deployment.discovery.jandex.Jandex;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test has an ancient Jandex version on classpath which normally triggers Jandex discovery strategy (and with this version
 * fails). Therefore, the test checks that with given option, reflection strategy is chosen and it won't crash.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class JandexDiscoveryStrategyDisabledTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        File oldJandex = Maven.resolver().resolve("org.jboss:jandex:1.0.3.Final").withTransitivity().asSingleFile();
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class).addPackage(JandexDiscoveryStrategyDisabledTest.class.getPackage()))
                .add(oldJandex) //add prehistoric Jandex to CP
                .addSystemProperty(Jandex.DISABLE_JANDEX_DISCOVERY_STRATEGY, "true") //disable jandex discovery strategy
                .build();
    }

    @Test
    public void testDeploymentWorksBecauseJandexIsNotUsed() {
        try (WeldContainer container = new Weld().initialize()) {
            Assert.assertTrue(container.isRunning());
        }
    }
}
