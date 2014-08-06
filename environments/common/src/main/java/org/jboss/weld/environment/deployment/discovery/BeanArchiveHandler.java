/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.deployment.discovery;

/**
 * Handles the reference to a bean archive.
 *
 * @author Martin Kouba
 */
public interface BeanArchiveHandler {

    /**
     * The returned builder must only contain a complete set of found classes, other properties do not have to be set.
     *
     * @param beanArchiveReference A reference to a bean archive (e.g. file path)
     * @return the BeanArchiveBuilder or <code>null</code> if the reference cannot be handled
     */
    BeanArchiveBuilder handle(String beanArchiveReference);

}
