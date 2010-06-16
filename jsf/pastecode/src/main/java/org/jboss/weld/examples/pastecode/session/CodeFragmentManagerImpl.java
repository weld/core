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

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.weld.examples.pastecode.model.CodeFragment;

@Stateless
public class CodeFragmentManagerImpl implements CodeFragmentManager
{

   // The number of code fragments to return in our recentCodeFragments query
   private static int MAX_RECENT_FRAGMENTS = 7;

   // The number of code fragments to display per page
   private static int PAGE_SIZE = 2;

   @PersistenceContext(unitName = "pastecodeDatabase")
   private EntityManager entityManager;

   @Inject
   private HashComputer hashComputer;

   public String addCodeFragment(CodeFragment code, boolean privateFragment)
   {
      // Set the defaults
      if (code.getDatetime() == null)
      {
         code.setDatetime(Calendar.getInstance().getTime());
      }

      if (code.getUser().trim().isEmpty())
      {
         code.setUser("Anonymous");
      }

      // compute hash value and return it if private flag has been set
      if (privateFragment)
      {
         try
         {
            String hashValue = hashComputer.getHashValue(code);
            code.setHash(hashValue);
            entityManager.persist(code);
            return hashValue;
         }
         catch (NoSuchAlgorithmException e)
         {
            e.printStackTrace();
            return null;
         }
      }
      // just return a non-hashed id
      else
      {
         entityManager.persist(code);
         return new Integer(code.getId()).toString();
      }
   }

   public CodeFragment getCodeFragment(String id)
   {
      // If it's not an integer, it's a hash!
      if (!isInteger(id))
      {
         Query query = entityManager.createQuery("SELECT c FROM CodeFragment c WHERE hash = :hash");
         query.setParameter("hash", id);

         @SuppressWarnings("unchecked")
         List<CodeFragment> fragments = query.getResultList();

         if (fragments.size() == 0)
         {
            throw new RuntimeException("No such fragment!");
         }
         else
         {
            return fragments.get(0);
         }
      }
      else
      {
         CodeFragment c = entityManager.find(CodeFragment.class, Integer.parseInt(id));
         if (c == null)
         {
            throw new RuntimeException("No such fragment!");
         }
         // If no hash was set, then this is not a private fragment, return it!
         if (c.getHash() == null)
         {
            return c;
         }
         else
         {
            throw new RuntimeException("Access denied!");
         }
      }
   }

   private static boolean isInteger(String string)
   {
      try
      {
         Integer.parseInt(string);
         return true;
      }
      catch (NumberFormatException e)
      {
         return false;
      }
   }

   public List<CodeFragment> getRecentCodeFragments()
   {
      Query query = entityManager.createQuery("SELECT c FROM CodeFragment c WHERE hash=null ORDER BY datetime DESC ");
      query.setMaxResults(MAX_RECENT_FRAGMENTS);

      @SuppressWarnings("unchecked")
      List<CodeFragment> codes = query.getResultList();

      return codes;
   }

   /**
    * getting codes from database needs new transaction so that we can further
    * modify returned Codes without affecting database (when we call this
    * function from another session bean
    */
   
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public List<CodeFragment> searchCodeFragments(CodeFragment code, int page, QueryInfo info)
   {
      StringBuilder sb = new StringBuilder();

      String delim = "";
      if (!code.getUser().trim().equals(""))
      {
         sb.append("c.user = \'" + code.getUser().trim().toLowerCase() + "\'");
         delim = " AND";
      }
      if (code.getLanguage() != null)
      {
         sb.append(delim).append(" c.language = \'" + code.getLanguage().name() + "\'");
         delim = " AND";
      }
      if (!code.getNote().trim().equals(""))
      {
         sb.append(delim).append(" c.note LIKE \'%" + code.getNote().trim().toLowerCase() + "%\'");
         delim = " AND";
      }
      if (!code.getText().trim().equals(""))
      {
         sb.append(delim).append(" c.text LIKE \'%" + code.getText().toLowerCase() + "%\'");
         delim = " AND";
      }
      if (code.getDatetime() != null)
      {
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
         Date date2 = new Date();
         date2.setTime(code.getDatetime().getTime() + 24 * 60 * 60 * 1000); // +1
         // day

         String formattedDate1 = formatter.format(code.getDatetime());
         String formattedDate2 = formatter.format(date2);

         sb.append(delim).append(" c.datetime between \'" + formattedDate1 + "\' and \'" + formattedDate2 + "\'");
         delim = " AND";
      }

      if (sb.toString().length() == 0)
         sb.append("1 = \'1\'");

      Query q = entityManager.createQuery("SELECT c FROM CodeFragment c WHERE hash=null AND " + sb.toString() + " ORDER BY datetime DESC");
      int allRecords = q.getResultList().size();
      q.setFirstResult(page * PAGE_SIZE);
      q.setMaxResults(PAGE_SIZE);
      
      @SuppressWarnings("unchecked")
      List<CodeFragment> codes = q.getResultList();

      info.setPage(page);
      info.setRecordsCount(allRecords);
      info.setPagesCount(allRecords / PAGE_SIZE);

      return codes;
   }
}
