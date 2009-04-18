/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
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


package org.jboss.webbeans.bootstrap.api;

/**
 * Holds a reference to an application singleton. This singleton is used 
 * internally by Web Beans to store various application scoped objects.
 * 
 * This allows Web Beans to operate as a shared library. In a shared mode, the 
 * same instance of WebBeans implementation is used by all the applications in 
 * a server environment. In the exclusive  mode, each application loads a 
 * separate copy of WebBeans implementation at the application level. 
 * 
 * Alternative implementations of Singleton can be used as required
 *
 * @see SingletonProvider
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Pete Muir
 */
public interface Singleton<T>
{
    /**
     * Access the singleton
     * 
     * @return a singleton object
     */
    public T get();

    /**
     * Store a singleton
     * 
     * @param object the object to store
     */
    public void set(T object);
}
