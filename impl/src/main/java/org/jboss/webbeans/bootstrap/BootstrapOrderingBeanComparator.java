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
      else if (o1.getType().getName().startsWith("org.jboss.webbeans") && !o2.getType().getName().startsWith("org.jboss.webbeans"))
      {
         return -1;
      }
      else if (!o1.getType().getName().startsWith("org.jboss.webbeans") && o2.getType().getName().startsWith("org.jboss.webbeans"))
      {
         return 1;
      }
      else if (o1 instanceof AbstractClassBean)
      {
         AbstractClassBean<?> b1 = (AbstractClassBean<?>) o1;
         if (o2 instanceof NewBean && !(o1 instanceof NewBean))
         {
            // Always initialize new beans after class beans
            return -1;
         }
         else if (o1 instanceof NewBean && o2 instanceof AbstractClassBean && !(o2 instanceof NewBean))
         {
            // Always initialize new beans after class beans
            return 1;
         }
         else if (o1 instanceof NewBean && !(o2 instanceof NewBean))
         {
            // Always initialize new class beans after class beans but before other beans
            return -1;
         }
         else if (o1 instanceof NewBean && o2 instanceof NewBean)
         {
            return o1.getId().compareTo(o2.getId());
         }
         else if (o2 instanceof AbstractClassBean)
         {
            AbstractClassBean<?> b2 = (AbstractClassBean<?>) o2;
            if (o1.getTypes().contains(b2.getType()))
            {
               return 1;
            }
            else if (b2.getTypes().contains(b1.getType()))
            {
               return -1;
            }
            else
            {
               return o1.getId().compareTo(o2.getId());
            }
         }
         else if (o2 instanceof AbstractProducerBean)
         {
            // Producer beans are always initialized after class beans
            return -1;
         }
         else
         {
            // Ordering doesn't matter
            return o1.getId().compareTo(o2.getId());
         }
      }
      else if (o1 instanceof AbstractProducerBean)
      {
         AbstractProducerBean<?, ?> b1 = (AbstractProducerBean<?, ?>) o1;
         if (o2 instanceof NewBean)
         {
            // Always initialize producers beans after new beans
            return 1;
         }
         else if (o2 instanceof AbstractClassBean)
         {
            if (b1.getDeclaringBean().equals(o2))
            {
               return 1;
            }
            else
            {
               return o1.getId().compareTo(o2.getId());
            }
         }
         else
         {
            // Ordering doesn't matter
            return o1.getId().compareTo(o2.getId());
         }
      }
      else
      {
         if (o2 instanceof AbstractClassBean || o2 instanceof AbstractProducerBean)
         {
            // Initialize undefined ordering after defined ordering
            return 1;
         }
         else
         {
            return o1.getId().compareTo(o2.getId());
         }
      }
   }
   
}
