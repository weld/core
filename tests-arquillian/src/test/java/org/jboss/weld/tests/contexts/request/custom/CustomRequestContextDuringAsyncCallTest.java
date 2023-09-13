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
package org.jboss.weld.tests.contexts.request.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.context.ejb.EjbRequestContext;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.contexts.request.custom.CustomContextExtension.CustomRequestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * The request context must be active during EJB async calls. If a custom context for {@link RequestScoped} is provided, Weld
 * should not activate {@link EjbRequestContext}.
 *
 * @author Martin Kouba
 * @see WELD-1880
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class CustomRequestContextDuringAsyncCallTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(CustomRequestContextDuringAsyncCallTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(
                        CustomRequestContextDuringAsyncCallTest.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class, CustomContextExtension.class);
    }

    @Inject
    SessionBean sessionBean;

    @Test
    public void testCustomRequestContext() throws InterruptedException, ExecutionException {
        Future<String> future = sessionBean.compute();
        String result = future.get();
        assertNotNull(result);
        assertEquals(CustomRequestContext.RESULT, result);
    }

}
