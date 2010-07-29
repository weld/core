/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.tests.interceptors.generic;

import java.util.Collection;
import java.util.List;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.test.AbstractWeldTest;
import org.jboss.weld.util.Beans;
import org.testng.annotations.Test;

/**
 * @author Marius Bogoevici
 */
@Artifact
public class SimpleWeldClassTest extends AbstractWeldTest
{

   @Test(groups = "broken")
   public void testWeldClassForGenericSuperclass()
   {
      WeldClass<StringProcessor> weldClass = WeldClassImpl.of(StringProcessor.class, new ClassTransformer(new TypeStore()));
      Collection methods = weldClass.getWeldMethods();
      //assert methods.size() == 2;
      List<WeldMethod<?,?>> interceptableMethods = Beans.getInterceptableMethods(weldClass);
      assert interceptableMethods.size() == 4;
   }

}
