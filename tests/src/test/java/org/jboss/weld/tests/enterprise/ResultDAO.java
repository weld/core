package org.jboss.weld.tests.enterprise;

import javax.ejb.Local;

@Local
public interface ResultDAO extends AbstractDAO<Result>
{

   public Result findByUser(String username);
   
}
