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
package org.jboss.weld.environment.servlet.test.bootstrap.configuration;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ForkJoinPool;

import jakarta.enterprise.inject.spi.CDI;

import org.jboss.weld.manager.api.WeldManager;
import org.junit.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.weld.bootstrap.ConcurrentValidator;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.events.ContainerLifecycleEvents;
import org.jboss.weld.executor.CommonForkJoinPoolExecutorServices;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CommonExecutorServicesConfigurationTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return baseDeployment()
                .addClass(CommonExecutorServicesConfigurationTest.class)
                .addAsResource(
                        PropertiesBuilder
                        .newBuilder()
                        .set("org.jboss.weld.executor.threadPoolType", "COMMON")
                        .build(), "weld.properties");
    }

    @Test
    public void testServices() throws Exception {
        // NOTE - this test cannot @Inject BeanManagerImpl due to different CL loading the BM class and injecting
        // this test. Hence, we use WeldManager resolved in this way.
        // see https://issues.redhat.com/browse/WELD-2519?focusedCommentId=13625216&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-13625216
        WeldManager manager = CDI.current().select(WeldManager.class).get();
        assertTrue(manager.getServices().get(Validator.class) instanceof ConcurrentValidator);
        assertTrue(manager.getServices().get(ContainerLifecycleEvents.class).isPreloaderEnabled());
        assertTrue(manager.getServices().get(ExecutorServices.class) instanceof CommonForkJoinPoolExecutorServices);
        CommonForkJoinPoolExecutorServices executor = (CommonForkJoinPoolExecutorServices) manager.getServices().get(ExecutorServices.class);
        Assert.assertTrue(executor.getTaskExecutor() == ForkJoinPool.commonPool());
    }
}
