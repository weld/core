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
