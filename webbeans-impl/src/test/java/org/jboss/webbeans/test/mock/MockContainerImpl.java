package org.jboss.webbeans.test.mock;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.webbeans.ContainerImpl;

public class MockContainerImpl extends ContainerImpl
{
   
   public MockContainerImpl(List<Annotation> enabledDeploymentTypes)
   {
      super(enabledDeploymentTypes);
   }

}
