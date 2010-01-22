/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.proxy.enterprise;

import javassist.util.proxy.ProxyObject;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@Packaging(PackagingType.EAR)
public class EnterpriseBeanProxyTest extends AbstractWeldTest
{
   
   /**
    * <a href="https://jira.jboss.org/jira/browse/WBRI-109">WBRI-109</a>
    */
   @Test(description="WBRI-109")
   public void testNoInterfaceView() throws Exception
   {
      Object mouse = getReference(MouseLocal.class);
      assert mouse instanceof ProxyObject;
      assert mouse instanceof MouseLocal;
   }
   
}
