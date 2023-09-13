/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;

import org.jboss.weld.manager.api.ExecutorServices;

@Priority(Interceptor.Priority.PLATFORM_AFTER + 400)
public class ExecutorServices1 implements ExecutorServices {

    @Override
    public void cleanup() {
    }

    @Override
    public ExecutorService getTaskExecutor() {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAllAndCheckForExceptions(Collection<? extends Callable<T>> tasks) {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAllAndCheckForExceptions(TaskFactory<T> factory) {
        return null;
    }

}
