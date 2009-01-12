package org.jboss.webbeans.ejb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

public class InternalEjbDescriptor<T> extends ForwardingEjbDescriptor<T> implements EjbDescriptor<T>
{
 
   private final Map<Class<?>, String> localBusinessInterfacesJndiNames;
   private final EjbDescriptor<T> delegate;
   
   public InternalEjbDescriptor(EjbDescriptor<T> ejbDescriptor)
   {
      this.delegate = ejbDescriptor;
      this.localBusinessInterfacesJndiNames = new HashMap<Class<?>, String>();
      for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces())
      {
         localBusinessInterfacesJndiNames.put(businessInterfaceDescriptor.getInterface(), businessInterfaceDescriptor.getJndiName());
      }
      // Internally, Object.class is added to the type hierachy of an 
      // EnterpriseBean, so we need to represent that here. We can just use any
      // of the local business interfaces
      localBusinessInterfacesJndiNames.put(Object.class, ejbDescriptor.getLocalBusinessInterfaces().iterator().next().getJndiName());
   }
   
   public Map<Class<?>, String> getLocalBusinessInterfacesJndiNames()
   {
      return Collections.unmodifiableMap(localBusinessInterfacesJndiNames);
   }
   
   @Override
   protected EjbDescriptor<T> delegate()
   {
      return delegate;
   }
   
}
