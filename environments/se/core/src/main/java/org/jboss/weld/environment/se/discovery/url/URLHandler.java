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
package org.jboss.weld.environment.se.discovery.url;

/**
 * Represents a handler for handling the url path to the directory of classes or to the jar files.
 *
 * @author Matej Briškár
 */
interface URLHandler {

    /**
     * Main method of the interface, handling the url path and filling the BeanArchiveBuilder instance.
     *
     * @return BeanArchiveBuilder containing the classes to be loaded, url to beans.xml of the archive and a created {@link org.jboss.jandex.Index} if jandex is
     *         enabled.
     */
    BeanArchiveBuilder handle(String urlPath);

    /**
     * @return true if the specific URLHandler implementation is able to hande the archrive with the given urlPath.
     */
    boolean canHandle(String urlPath);
}
