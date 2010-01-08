/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.weld.tests.decorators.abstractDecorator.broken;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.ExpectedDeploymentException;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
@Artifact
@BeansXml("beans-withInvalidAbstractMethod.xml")
@ExpectedDeploymentException(DefinitionException.class)
public class SimpleAbstractDecoratorWithInvalidAbstractMethodTest extends AbstractWeldTest
{
   @Test(groups = "broken")
   // TODO: needs exception transformer to run in JBAS
   public void testAbstractDecoratorApplied()
   {
      assert false;
   }

}