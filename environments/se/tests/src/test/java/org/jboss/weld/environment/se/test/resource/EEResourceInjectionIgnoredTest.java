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
package org.jboss.weld.environment.se.test.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.enterprise.util.TypeLiteral;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bean.builtin.InstanceImpl;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 *
 */
@RunWith(Arquillian.class)
public class EEResourceInjectionIgnoredTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(ShrinkWrap.create(BeanArchive.class).addClasses(EEResourceInjectionIgnoredTest.class, 
                                                                                       Golf.class, Delta.class, AsyncValue.class, AsyncValueImpl.class,
                                                                                       AsyncProducer.class)).build();
    }

    @Test
    public void testInjection() throws InterruptedException, ExecutionException {
        try (WeldContainer container = new Weld().initialize()) {
        	  WeldInstance<Golf> instance = container.select(Golf.class);
        	  System.err.println("Select async");
        	  CompletableFuture<Void> future = ((InstanceImpl<Golf>)instance).getAsync().thenAccept(golf -> {
        	    System.err.println("Got async value: "+golf);
        	    assertNull(golf.getEntityManager());
        	    assertEquals(10, golf.getDelta().ping());
              assertEquals(10, golf.getAsyncValue().ping());
        	  }).toCompletableFuture();
            System.err.println("Done asking");
            
            // Either produce it ourselves, or produce it async in AsyncProducer
            CompletionStage<AsyncValue> valueFuture = container.select(new TypeLiteral<CompletionStage<AsyncValue>>(){}).get();
            valueFuture.toCompletableFuture().complete(new AsyncValueImpl(null));
            
            System.err.println("Now waiting");
        	  future.get();
            System.err.println("Waiting done");
        }
    }

}
