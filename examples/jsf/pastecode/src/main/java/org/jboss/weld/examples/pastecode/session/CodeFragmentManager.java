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

import org.jboss.weld.examples.pastecode.model.CodeFragment;

import javax.ejb.Local;
import java.util.List;

/**
 * Retrieval and addition of code fragments
 *
 * @author Pete Muir
 * @author Martin Gencur
 */
@Local
public interface CodeFragmentManager {
    /**
     * The number of code fragments to return in our recentCodeFragments query
     */
    int MAX_RECENT_FRAGMENTS = 7;

    /**
     * Add the code fragment, computing a hash for it's id if it is private,
     * otherwise, using a sequential id
     */
    String addCodeFragment(CodeFragment code, boolean privateFragment);

    /**
     * Retrieve a code fragment, using it's id
     */
    CodeFragment getCodeFragment(String id);

    /**
     * Get the {@value #MAX_RECENT_FRAGMENTS} most recent fragments
     */
    List<CodeFragment> getRecentCodeFragments();

    /**
     * Find code fragments by example. Supports paging.
     */
    List<CodeFragment> searchCodeFragments(CodeFragment code, int page, Paginator info);
}
