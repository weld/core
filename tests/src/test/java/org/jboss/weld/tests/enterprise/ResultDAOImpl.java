package org.jboss.weld.tests.enterprise;

import javax.ejb.Stateless;

@DAO
@Stateless
public class ResultDAOImpl extends AbstractDAOImpl<Result> implements ResultDAO
{
   public Result findByUser(String username)
   {
      return new Result(username);
   }
}