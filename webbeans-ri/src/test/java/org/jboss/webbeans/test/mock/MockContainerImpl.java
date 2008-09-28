package org.jboss.webbeans.test.mock;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.webbeans.ManagerImpl;

public class MockContainerImpl extends ManagerImpl
{
   
   public MockContainerImpl(List<Annotation> enabledDeploymentTypes)
   {
      super(enabledDeploymentTypes);
   }

}
