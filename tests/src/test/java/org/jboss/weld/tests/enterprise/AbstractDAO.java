package org.jboss.weld.tests.enterprise;

public interface AbstractDAO<E>
{
   public boolean save(E entity);
   
   public boolean isSaved();
   
}