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

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * The view controller for the history screen
 *
 * @author Martin Gencur
 * @author Pete Muir
 */
@SessionScoped
@Named("history")
public class History implements Serializable {

    private static final long serialVersionUID = 20L;

    @Inject
    private CodeFragmentManager codeFragmentManager;

    private Paginator paginator;

    private List<CodeFragment> codes;

    // The Search we are conducting
    private final CodeFragment codeFragmentPrototype;

    private int page = 0;

    public History() {
        this.codeFragmentPrototype = new CodeFragment();
    }

    public List<CodeFragment> getCodes() {
        return this.codes;
    }

    public CodeFragment getCodeFragmentPrototype() {
        return codeFragmentPrototype;
    }

    //Start a *new* search!
    public String newSearch() {
        this.page = 0;
        return "history";
    }

    // Do the search, called as a "page action"
    public String load() {
        this.paginator = new Paginator();
        this.codes = null;

        // Perform a search

        this.codes = codeFragmentManager.searchCodeFragments(this.codeFragmentPrototype, this.page, this.paginator);
        return "history";
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }
}
