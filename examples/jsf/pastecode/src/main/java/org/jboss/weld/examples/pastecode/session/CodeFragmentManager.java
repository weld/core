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
