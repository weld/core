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
package org.jboss.weld.tests.scope.unproxyable;

import javax.enterprise.inject.UnproxyableResolutionException;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.ExpectedDeploymentException;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
@ExpectedDeploymentException(UnproxyableResolutionException.class)
public class UnproxyableTest extends AbstractWeldTest
{
   
   @Test(groups = "incontainer-broken")
   //JBoss AS is producing the wrong exception.
   public void test()
   {
      
   }

}
