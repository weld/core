package org.jboss.webbeans.test.unit.implementation.event.tx;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;

@Named
@ApplicationScoped
public class Foo implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Inject
   private UserTransaction utx;
   
   @Inject @Updated
   private Event<String> event1;

   public Foo()
   {
   }

   public String save()
   {
      try
      {
         utx.begin();
         event1.fire("Fired using Event Interface with AnnotationLiteral.");
         utx.commit();
      }
      catch (Exception e)
      {
      }
      return null;
   }

   public void eventObserver(@Observes(during = TransactionPhase.AFTER_COMPLETION) @Updated String s)
   {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("AfterCompletion Event Recieved!"));
   }
}