/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.examples.pastecode.session;

import java.util.Date;
import javax.annotation.Resource;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.weld.examples.pastecode.model.CodeEntity;
import org.jboss.weld.examples.pastecode.model.LargeCodeLog;
import javax.transaction.UserTransaction;

/**
 * This Decorator performs logging of information about large
 * transactions. A transaction is large if the pasted code has
 * more than 65kB. When such a transaction is performed the 
 * following pieces of information are being recorded:
 * - id of the transaction, 
 * - time of the transaction
 * - "w" - if it was written, "r" - if it was read
 *  
 */
@Decorator
@TransactionManagement(TransactionManagementType.BEAN)
public abstract class LargeCodeDecorator implements Code
{
   /* injecting Delagation point - mandatory */
   @Inject @Delegate @Any Code eao; 

   @PersistenceContext(unitName = "pastecodeDatabase")
   private EntityManager em;
   
   @Resource UserTransaction ut;
   
   private long LARGE_CODE = 65536;
   
   public String addCode(CodeEntity code, boolean secured)
   {
      
      String codeId = eao.addCode(code, secured);
      
      if (code.getText().length() > LARGE_CODE)
      {
         try
         {
            ut.begin();
            em.joinTransaction();
            em.persist(new LargeCodeLog(code.getId(), code.getDatetime(), "w")); //writing large code
            ut.commit();
         }
         catch(Exception e)
         {
            e.printStackTrace();
            try
            {
               ut.rollback();
            }
            catch (Exception ex)
            {               
            }
         }
      }
      
      return codeId;
   }

   public CodeEntity getCode(String id)
   {
      CodeEntity code = eao.getCode(id);
      
      if (code.getText().length() > LARGE_CODE)
      {
         try
         {
            ut.begin();
            em.joinTransaction();
            em.persist(new LargeCodeLog(code.getId(), new Date(), "r")); //reading large code
            ut.commit();
         }
         catch(Exception e)
         {
            e.printStackTrace();
            try
            {
               ut.rollback();
            }
            catch (Exception ex)
            {               
            }
         }
      }
      
      return code;
   }
}
