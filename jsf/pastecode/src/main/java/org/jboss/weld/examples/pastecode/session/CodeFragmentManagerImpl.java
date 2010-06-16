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
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
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

   @PersistenceContext
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

   @Produces @Named
   public List<CodeFragment> getRecentCodeFragments()
   {
      Query query = entityManager.createQuery("SELECT c FROM CodeFragment c WHERE c.hash=null ORDER BY datetime DESC ");
      query.setMaxResults(MAX_RECENT_FRAGMENTS);

      @SuppressWarnings("unchecked")
      List<CodeFragment> codes = query.getResultList();

      return codes;
   }

   public List<CodeFragment> searchCodeFragments(CodeFragment code, int page, Paginator paginator)
   {
      StringBuilder sb = new StringBuilder().append("SELECT c FROM CodeFragment c WHERE c.hash=null");
      
      if (!code.getUser().trim().equals(""))
      {
         sb.append(" AND c.user = \'").append(code.getUser().trim().toLowerCase()).append("\'");
      }
      if (code.getLanguage() != null)
      {
         sb.append(" AND c.language = \'").append(code.getLanguage().name()).append(("\'"));
      }
      if (!code.getNote().trim().equals(""))
      {
         sb.append(" AND c.note LIKE \'%").append(code.getNote().trim().toLowerCase()).append("%\'");
      }
      if (!code.getText().trim().equals(""))
      {
         sb.append(" AND c.text LIKE \'%").append(code.getText().toLowerCase()).append("%\'");
      }
      if (code.getDatetime() != null)
      {
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
         Date date2 = new Date();
         date2.setTime(code.getDatetime().getTime() + 24 * 60 * 60 * 1000); // +1
         // day

         String formattedDate1 = formatter.format(code.getDatetime());
         String formattedDate2 = formatter.format(date2);

         sb.append(" AND c.datetime between \'").append(formattedDate1).append("\' and \'").append(formattedDate2).append("\'");
      }
      sb.append(" ORDER BY datetime DESC");
      String queryString = sb.toString();
      
      Query q = entityManager.createQuery(queryString);
      
      q.setFirstResult(page * PAGE_SIZE);
      q.setMaxResults(PAGE_SIZE);
      
      @SuppressWarnings("unchecked")
      List<CodeFragment> codes = q.getResultList();

      paginator.setPage(page);
      paginator.setRecordsCount(codes.size());
      paginator.setPagesCount(codes.size() / PAGE_SIZE);

      return codes;
   }
}
