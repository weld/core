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

package org.jboss.weld.tests.proxy.instantiator;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WELD-687.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class PerDeploymentInstantiatorWithTest extends AbstractPerDeploymentInstantiator {

    @Deployment
    public static WebArchive getDeploymentWith() {
        return getDeployment(PerDeploymentInstantiatorWithTest.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "classes/META-INF/org.jboss.weld.enableUnsafeProxies")
                .addAsManifestResource(EmptyAsset.INSTANCE, "org.jboss.weld.enableUnsafeProxies"); // workaround ARQ-753
    }

    @Inject
    InjectedBean bean;

    @Test
    public void testWith() throws Exception {
        bean.getUnproxiable().getBm();
    }

}
