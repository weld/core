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
package org.jboss.weld.tests.extensions.annotatedType.ejb;

import javax.enterprise.util.AnnotationLiteral;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Packaging;
import org.jboss.testharness.impl.packaging.PackagingType;
import org.jboss.testharness.impl.packaging.jsr299.Extension;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;
/**
 * Tests that it is possible to override ejb annotations through the SPI
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 *
 */
@Artifact
@Packaging(PackagingType.EAR)
@IntegrationTest
@Extension("javax.enterprise.inject.spi.Extension")
@Classes(packages = { "org.jboss.weld.tests.util.annotated" })
public class AnnotatedTypeSessionBeanTest extends AbstractWeldTest
{
   @Test
   public void testOverridingEjbAnnotations()
   {
      Shaft conveyerShaft = getReference(Shaft.class, new AnnotationLiteral<ConveyorShaft>() { });
      assert conveyerShaft != null;
   }
   
   @Test
   public void testAddingBultipleBeansPerEjbClass()
   {
      LatheLocal bigLathe = getReference(LatheLocal.class, new AnnotationLiteral<BigLathe>() { });
      assert bigLathe != null;
      LatheLocal smallLathe = getReference(LatheLocal.class, new AnnotationLiteral<SmallLathe>() { });
      assert smallLathe != null;
   }
}
