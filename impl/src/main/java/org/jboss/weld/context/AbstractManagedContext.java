package org.jboss.weld.context;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public abstract class AbstractManagedContext extends AbstractContext implements ManagedContext
{

   private final ThreadLocal<Boolean> active;
   private final ThreadLocal<Boolean> valid;

   public AbstractManagedContext(boolean multithreaded)
   {
      super(multithreaded);
      this.active = new ThreadLocal<Boolean>()
      {
         
         @Override
         protected Boolean initialValue()
         {
            return FALSE;
         }
         
      };
      this.valid = new ThreadLocal<Boolean>()
      {
         
         protected Boolean initialValue() 
         {
            return TRUE;
         }
         
      };
      
   }

   public boolean isActive()
   {
      return active.get().booleanValue();
   }

   protected void setActive(boolean active)
   {
      this.active.set(active);
   }

   public void invalidate()
   {
      this.valid.set(FALSE);
   }

   public void activate()
   {
      setActive(true);
   }

   public void deactivate()
   {
      if (!valid.get().booleanValue())
      {
         destroy();
      }
      setActive(false);
   }

   @Override
   public void cleanup()
   {
      super.cleanup();
      active.remove();
      valid.remove();
   }

}