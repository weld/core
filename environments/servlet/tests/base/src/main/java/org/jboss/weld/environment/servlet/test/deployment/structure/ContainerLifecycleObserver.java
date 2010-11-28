package org.jboss.weld.environment.servlet.test.deployment.structure;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

public class ContainerLifecycleObserver implements Extension
{

   private List<AnnotatedType<?>> processedAnnotatedTypes;
   
   public ContainerLifecycleObserver()
   {
      processedAnnotatedTypes = new ArrayList<AnnotatedType<?>>();
   }
   
   public void observeProcessFoo(@Observes ProcessAnnotatedType<Foo> event)
   {
      this.processedAnnotatedTypes.add(event.getAnnotatedType());
   }
   
   public void observeProcessBaz(@Observes ProcessAnnotatedType<Baz> event)
   {
      this.processedAnnotatedTypes.add(event.getAnnotatedType());
   }
   
   public List<AnnotatedType<?>> getProcessedAnnotatedTypes()
   {
      return processedAnnotatedTypes;
   }
   
}
