/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.se.test.extension.build.compatible.none;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.lite.extension.translator.LiteExtensionTranslator;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that in case when there is no Build Compatible Extension, the {@code LiteExtensionTranslator} won't be
 * added to the deployment.
 */
@RunWith(Arquillian.class)
public class NoExtensionFoundTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(NoExtensionFoundTest.class))
                        .addPackage(NoExtensionFoundTest.class.getPackage()))
                .build();
    }

    @Test
    public void testNoBCEFound() {
        try (WeldContainer container = new Weld().initialize()) {
            // assert the deployment is fine, DummyBean should be resolvable
            Assert.assertTrue(container.select(DummyBean.class).isResolvable());

            // no BCE is added to the deployment so LiteExtensionTranslator shouldn't be added either
            Assert.assertFalse(container.select(LiteExtensionTranslator.class).isResolvable());
        }
    }
}
