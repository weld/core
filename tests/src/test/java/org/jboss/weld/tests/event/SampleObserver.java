package org.jboss.weld.tests.event;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@SessionScoped
public class SampleObserver implements Serializable
{

   private static final long serialVersionUID = -8311790045944819159L;

   @Inject
   private Qux qux;
   
   private boolean injectionAndObservationOccured;

   @SuppressWarnings("unused")
   private void observes(@Observes final Baz baz)
   {
      injectionAndObservationOccured = qux != null;
   }
   
   public boolean isInjectionAndObservationOccured()
   {
      return injectionAndObservationOccured;
   }
   
}