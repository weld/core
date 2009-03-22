package org.jboss.webbeans.bootstrap;

import java.util.Comparator;

import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.AbstractProducerBean;
import org.jboss.webbeans.bean.NewBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.standard.AbstractStandardBean;

public class BootstrapOrderingBeanComparator implements Comparator<RIBean<?>>
{

   public int compare(RIBean<?> o1, RIBean<?> o2)
   {
      if (o1 instanceof AbstractClassBean && o2 instanceof AbstractProducerBean)
      {
//         AbstractProducerBean<?, ?> b2 = (AbstractProducerBean<?, ?>) o2;
//         System.out.println("  declaring bean " + b2.getDeclaringBean().getType());
//         if (b2.getDeclaringBean().equals(o1))
//         {
//            System.out.println("  class " + o1.getType() + " < producer " + o2.getType());
//            return -1;
//         }
         return -1;
      }
      else if (o1 instanceof AbstractProducerBean && o2 instanceof AbstractClassBean)
      {
//         AbstractProducerBean<?, ?> b1 = (AbstractProducerBean<?, ?>) o1;
//         System.out.println("  declaring bean " + b1.getDeclaringBean().getType());
//         if (b1.getDeclaringBean().equals(o2))
//         {
//            System.out.println("  class " + o2.getType() + " < producer " + o1.getType());
//            return 1;
//         }
         return 1;
      }
      
      if (o1 instanceof AbstractClassBean && o2 instanceof AbstractClassBean)
      {
         AbstractClassBean<?> b1 = (AbstractClassBean<?>) o1;
         AbstractClassBean<?> b2 = (AbstractClassBean<?>) o2;
         if (b1.getSuperclasses().contains(b2.getType().getName()))
         {
            // Place o1 after it's superclass
            return 1;
         }
         else if (b2.getSuperclasses().contains(b1.getType().getName()))
         {
            // Place o1 before it's subclass o2
            return -1;
         }
      }
      
      if (o1 instanceof AbstractStandardBean && !(o2 instanceof AbstractStandardBean))
      {
         return -1;
      }
      else if (!(o1 instanceof AbstractStandardBean) && o2 instanceof AbstractStandardBean)
      {
         return 1;
      }
      else if (o1 instanceof AbstractStandardBean && o2 instanceof AbstractStandardBean)
      {
         return o1.getId().compareTo(o2.getId());
      }
      
      if (o1.getType().getName().startsWith("org.jboss.webbeans") && !o2.getType().getName().startsWith("org.jboss.webbeans"))
      {
         return -1;
      }
      else if (!o1.getType().getName().startsWith("org.jboss.webbeans") && o2.getType().getName().startsWith("org.jboss.webbeans"))
      {
         return 1;
      }
      
      if (!(o1 instanceof NewBean) && o2 instanceof NewBean)
      {
         return -1;
      }
      else if (o1 instanceof NewBean && !(o2 instanceof NewBean))
      {
         return 1;
      }
      
      return o1.getId().compareTo(o2.getId());
   }
   
}
