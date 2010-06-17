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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.weld.examples.pastecode.model.CodeFragment;
import org.jboss.weld.examples.pastecode.model.CodeFragment_;

@Stateless
public class CodeFragmentManagerImpl implements CodeFragmentManager
{
   
   @Inject Logger log;

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
            log.info("Added private pastecode: " + hashValue);
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
         // Make sure we have the latest version (with the generated id!)
         entityManager.refresh(code);
         log.info("Added pastecode: " + code.getId());
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
   
   private static boolean isEmpty(String string)
   {
      return string == null || string.equals("");
   }

   public List<CodeFragment> searchCodeFragments(CodeFragment codeFragment, int page, Paginator paginator)
   {
      
      CriteriaBuilder builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<CodeFragment> criteria = builder.createQuery(CodeFragment.class);
      
      Root<CodeFragment> root = criteria.from(CodeFragment.class);
      
      List<Predicate> predicates = new ArrayList<Predicate>();
      
      predicates.add(builder.isNull(root.get(CodeFragment_.hash)));

      if (!isEmpty(codeFragment.getUser()))
      {
         predicates.add( builder.equal(root.get(CodeFragment_.user), codeFragment.getUser().toLowerCase().trim()) );
      }
      if (codeFragment.getLanguage() != null)
      {
         predicates.add( builder.equal(root.get(CodeFragment_.language), codeFragment.getLanguage()) );
      }
      if (!isEmpty(codeFragment.getNote()))
      {
         predicates.add( builder.like(root.get(CodeFragment_.note), codeFragment.getNote().toLowerCase()) );
      }
      if (!isEmpty(codeFragment.getText()))
      {
         predicates.add( builder.like(root.get(CodeFragment_.text), codeFragment.getText().toLowerCase()) );
      }
      if (codeFragment.getDatetime() != null)
      {
         predicates.add( builder.between(root.get(CodeFragment_.datetime), codeFragment.getDatetime(), new Date()) );
      }
      
      criteria.where(predicates.toArray(new Predicate[0])).orderBy(builder.desc(root.get(CodeFragment_.datetime)));
      
      Query q = entityManager.createQuery(criteria);
      
      int totalRecords = q.getResultList().size();
      
      q.setFirstResult(page * PAGE_SIZE);
      q.setMaxResults(PAGE_SIZE);
      
      @SuppressWarnings("unchecked")
      List<CodeFragment> codes = q.getResultList();

      paginator.setPage(page);
      paginator.setRecordsCount(totalRecords);
      paginator.setPagesCount(totalRecords / PAGE_SIZE);

      return codes;
   }
}
