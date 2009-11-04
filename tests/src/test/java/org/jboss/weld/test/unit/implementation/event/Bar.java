package org.jboss.weld.test.unit.implementation.event;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

@RequestScoped
public class Bar
{

   @Inject Event<String> event;
   
   @Inject @Updated Event<String> updatedEvent;
   
   @Inject BeanManager manager;
   
   private boolean unqualifiedObserved;
   private boolean updatedObserved;

   public void fireWithNoQualifiers()
   {
      event.fire("");
   }
   
   public void fireWithUpdatedQualifierViaSelect()
   {
      event.select(new AnnotationLiteral<Updated>() {}).fire("");
   }
   
   public void fireWithNoQualifiersViaManager()
   {
      manager.fireEvent("");
   }
   
   public void fireWithUpdatedQualifierViaManager()
   {
      manager.fireEvent("", new AnnotationLiteral<Updated>() {});
   }
   
   public void fireWithUpdatedQualifierViaAnnotation()
   {
      updatedEvent.fire("");
   }
   
   public void reset()
   {
      unqualifiedObserved = false;
      updatedObserved = false;
   }

   public void onEvent(@Observes String event)
   {
      unqualifiedObserved = true;
   }

   public void onUpdatedEvent(@Observes @Updated String event)
   {
      updatedObserved = true;
   }
   
   public boolean isUnqualifiedObserved()
   {
      return unqualifiedObserved;
   }
   
   public boolean isUpdatedObserved()
   {
      return updatedObserved;
   }
   
}