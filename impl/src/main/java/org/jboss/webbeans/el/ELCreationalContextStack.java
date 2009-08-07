package org.jboss.webbeans.el;

import java.util.Stack;

import javax.el.ELContext;

class ELCreationalContextStack extends Stack<ELCreationalContext<?>>
{
   
   private static final long serialVersionUID = -57142365866995726L;
   
   public static ELCreationalContextStack addToContext(ELContext context)
   {
      ELCreationalContextStack store = new ELCreationalContextStack();
      context.putContext(ELCreationalContextStack.class, store);
      return store;
   }
   
}