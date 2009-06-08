package org.jboss.webbeans.el;

import javax.el.ELResolver;

import org.jboss.webbeans.CurrentManager;

public class WebBeansELResolver extends ForwardingELResolver
{

   @Override
   protected ELResolver delegate()
   {
      return CurrentManager.rootManager().getCurrent().getELResolver();
   }

}
