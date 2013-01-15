/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.examples.pastecode.session;

import org.jboss.weld.examples.pastecode.model.CodeFragment;
import org.jboss.weld.examples.pastecode.model.CodeFragment_;

import javax.ejb.Stateful;
import javax.enterprise.event.Event;
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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link CodeFragmentManager}
 *
 * @author Pete Muir
 * @author Martin Gencur
 */
@Stateful
public class CodeFragmentManagerImpl implements CodeFragmentManager {

    // The number of code fragments to display per page
    private static int PAGE_SIZE = 2;

    @Inject
    private Event<CodeFragment> event;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private HashComputer hashComputer;

    public String addCodeFragment(CodeFragment code, boolean privateFragment) {
        // Set the defaults
        if (code.getDatetime() == null) {
            code.setDatetime(Calendar.getInstance().getTime());
        }

        if (code.getUser().trim().isEmpty()) {
            code.setUser("Anonymous");
        }

        // compute hash value and return it if private flag has been set
        if (privateFragment) {
            try {
                String hashValue = hashComputer.getHashValue(code);
                code.setHash(hashValue);
                entityManager.persist(code);
                event.fire(code);
                return hashValue;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
        // just return a non-hashed id
        else {
            entityManager.persist(code);
            entityManager.flush();
            event.fire(code);
            // Make sure we have the latest version (with the generated id!)
            entityManager.refresh(code);
            return new Integer(code.getId()).toString();
        }
    }

    public CodeFragment getCodeFragment(String id) {
        // If it's not an integer, it's a hash!
        if (!isInteger(id)) {
            Query query = entityManager.createQuery("SELECT c FROM CodeFragment c WHERE hash = :hash");
            query.setParameter("hash", id);

            @SuppressWarnings("unchecked")
            List<CodeFragment> fragments = query.getResultList();

            if (fragments.size() == 0) {
                throw new RuntimeException("No such fragment!");
            } else {
                return fragments.get(0);
            }
        } else {
            CodeFragment c = entityManager.find(CodeFragment.class, Integer.parseInt(id));
            if (c == null) {
                throw new RuntimeException("No such fragment!");
            }
            // If no hash was set, then this is not a private fragment, return it!
            if (c.getHash() == null) {
                return c;
            } else {
                throw new RuntimeException("Access denied!");
            }
        }
    }

    private static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Produces
    @Named
    public List<CodeFragment> getRecentCodeFragments() {
        Query query = entityManager.createQuery("SELECT c FROM CodeFragment c WHERE c.hash=null ORDER BY datetime DESC ");
        query.setMaxResults(MAX_RECENT_FRAGMENTS);

        @SuppressWarnings("unchecked")
        List<CodeFragment> codes = query.getResultList();

        return codes;
    }

    private static boolean isEmpty(String string) {
        return string == null || string.equals("");
    }

    public List<CodeFragment> searchCodeFragments(CodeFragment codeFragment, int page, Paginator paginator) {
        // Create a criteria, which we then populate using our prototype code fragment
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CodeFragment> criteria = builder.createQuery(CodeFragment.class);

        Root<CodeFragment> root = criteria.from(CodeFragment.class);

        List<Predicate> predicates = new ArrayList<Predicate>();

        // Only search public code fragements
        predicates.add(builder.isNull(root.get(CodeFragment_.hash)));

        if (!isEmpty(codeFragment.getUser())) {
            predicates.add(builder.equal(root.get(CodeFragment_.user), codeFragment.getUser().toLowerCase().trim()));
        }
        if (codeFragment.getLanguage() != null) {
            predicates.add(builder.equal(root.get(CodeFragment_.language), codeFragment.getLanguage()));
        }
        if (!isEmpty(codeFragment.getText())) {
            predicates.add(builder.like(root.get(CodeFragment_.text), "%" + codeFragment.getText().toLowerCase().trim() + "%"));
        }
        if (codeFragment.getDatetime() != null) {
            predicates.add(builder.between(root.get(CodeFragment_.datetime), codeFragment.getDatetime(), new Date()));
        }

        criteria.where(predicates.toArray(new Predicate[0])).orderBy(builder.desc(root.get(CodeFragment_.datetime)));

        Query q = entityManager.createQuery(criteria);

        int totalRecords = q.getResultList().size();


        // Compute the page
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
