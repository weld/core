package org.jboss.weld.tests.enterprise;


public abstract class AbstractDAOImpl<E>
{
   
   private boolean saved;

   public boolean save(E entity)
   {
      this.saved = true;
      return true;
   }
   
   public boolean isSaved()
   {
      return saved;
   }
   
}
