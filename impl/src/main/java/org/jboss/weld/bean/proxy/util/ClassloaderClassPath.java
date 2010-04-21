/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javassist.ClassPath;
import javassist.NotFoundException;

/**
 * A special classpath type used with the high-level Javassist API to lookup
 * class bytecode through a specific classloader.  This avoids the problem
 * of finding classes in Javassist when it is used in JEE containers.
 * 
 * @author David Allen
 *
 */
public class ClassloaderClassPath implements ClassPath
{
   private final ClassLoader classLoader;
   
   public ClassloaderClassPath(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }
   
   public void close()
   {
   }

   public URL find(String classname)
   {
      String resourceName = classname.replace('.', File.separatorChar) + ".class";
      return classLoader.getResource(resourceName);
   }

   public InputStream openClassfile(String classname) throws NotFoundException
   {
      String resourceName = classname.replace('.', File.separatorChar) + ".class";
      return classLoader.getResourceAsStream(resourceName);
   }

}
