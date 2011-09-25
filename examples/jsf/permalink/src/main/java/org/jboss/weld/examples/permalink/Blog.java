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
package org.jboss.weld.examples.permalink;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dan Allen
 */
@Model
public class Blog {
    private static final int PAGE_SIZE = 3;

    @Inject
    Repository repository;

    private Long entryId;

    private String category;

    private BlogEntry entry;

    private List<BlogEntry> entriesForPage;

    private List<String> categories;

    private boolean nextPageAvailable;

    private int page = 1;

    private String searchString;

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSearchString() {
        // return null to prevent page param from being encoded when not necessary
        return searchString == null || searchString.length() == 0 ? null : searchString;
    }

    public void setSearchString(String searchString) {
        if (searchString != null) {
            searchString = searchString.trim();
        }
        this.searchString = searchString;
    }

    public Integer getPage() {
        // return null to prevent page param from being encoded when not necessary
        return page == 1 ? null : page;
    }

    public void setPage(Integer page) {
        // NOTE if we were to use a primitive propery, page parameters would fail if value is null
        if (page == null) {
            this.page = 1;
        } else if (page < 1) {
            throw new IllegalArgumentException("Page must be greater than or equal to 1");
        } else {
            this.page = page;
        }
    }

    public void loadCategories() {
        categories = retrieveCategories();
    }

    /**
     * Init method for the main blog page
     */
    public void loadLatestEntries() {
        loadCategories();
        entriesForPage = (searchString != null ? retrieveSearchResults() : retrieveLatestEntries());
    }

    /**
     * Init method for a category page
     */
    public void loadLatestEntriesInCategory() {
        loadCategories();
        if (categories.contains(category)) {
            entriesForPage = (searchString != null ? retrieveSearchResultsInCategory() : retrieveLatestEntriesInCategory());
        } else {
            category = null;
        }
    }

    /**
     * Init method for an entry page
     */
    public void loadEntry() {
        loadCategories();
        entry = retrieveSelectedEntry();
    }

    public boolean search() {
        page = 1;
        return true;
    }

    /**
     * Retrieves the catagories used in this blog. This method references preloaded
     * data and is intended to be used in the EL value expressions in the view template.
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * Retrieves the categories used in this blog other than the selected one. This
     * method references preloaded data and is intended to be used in the EL value
     * expressions in the view template.
     */
    public List<String> getOtherCategories() {
        List<String> others = new ArrayList<String>();
        // defensive here because of postback decodes grrr..
        if (categories != null) {
            others.addAll(categories);
            others.remove(category);
        }
        return others;
    }

    /**
     * Retrieves the entries loaded for this page. This method references preloaded
     * data and is intended to be used in EL value expressions in the view template.
     */
    public List<BlogEntry> getEntriesForPage() {
        return entriesForPage;
    }

    public Set<BlogEntry> getEntriesForPageAsSet() {
        return new LinkedHashSet(entriesForPage);
    }

    public int getNumEntriesOnPage() {
        return entriesForPage.size();
    }

    /**
     * Retrieves the entry loaded for this page. This method references preloaded
     * data and is intended to be used in EL value expressions in the view template.
     */
    public BlogEntry getEntry() {
        return entry;
    }

    public int getPreviousPageWithFirstPageAsNumber() {
        assert page > 1;
        return page - 1;
    }

    public Integer getPreviousPage() {
        assert page > 1;
        return page > 2 ? page - 1 : null;
    }

    public int getPageWithFirstPageAsNumber() {
        return page;
    }

    public int getNextPage() {
        return page + 1;
    }

    public boolean isNextPageAvailable() {
        return nextPageAvailable;
    }

    public boolean isPreviousPageAvailable() {
        return page > 1;
    }

    protected List<BlogEntry> retrieveLatestEntries() {
        List<BlogEntry> entries = repository.getLatestEntries((page - 1) * PAGE_SIZE, PAGE_SIZE + 1);
        if (entries.isEmpty() && page > 1) {
            page = 1;
            entries = repository.getLatestEntries(0, PAGE_SIZE + 1);
        }

        return postProcessNavigationProbe(entries);
    }

    protected List<BlogEntry> retrieveSearchResults() {
        List<BlogEntry> entries = repository.searchEntries(searchString, (page - 1) * PAGE_SIZE, PAGE_SIZE + 1);
        if (entries.isEmpty() && page > 1) {
            page = 1;
            entries = repository.searchEntries(searchString, 0, PAGE_SIZE + 1);
        }

        return postProcessNavigationProbe(entries);
    }

    protected List<BlogEntry> retrieveLatestEntriesInCategory() {
        List<BlogEntry> entries = repository.getLatestEntries(category, (page - 1) * PAGE_SIZE, PAGE_SIZE + 1);
        if (entries.isEmpty() && page > 1) {
            page = 1;
            entries = repository.getLatestEntries(category, 0, PAGE_SIZE + 1);
        }

        return postProcessNavigationProbe(entries);
    }

    protected List<BlogEntry> retrieveSearchResultsInCategory() {
        List<BlogEntry> entries = repository.searchEntries(searchString, category, (page - 1) * PAGE_SIZE, PAGE_SIZE + 1);
        if (entries.isEmpty() && page > 1) {
            page = 1;
            entries = repository.searchEntries(searchString, category, 0, PAGE_SIZE + 1);
        }

        return postProcessNavigationProbe(entries);
    }

    private List<BlogEntry> postProcessNavigationProbe(List<BlogEntry> entries) {
        if (entries.size() > PAGE_SIZE) {
            nextPageAvailable = true;
            entries.remove(entries.size() - 1);
        } else {
            nextPageAvailable = false;
        }

        return entries;
    }

    protected BlogEntry retrieveSelectedEntry() {
        return repository.getEntry(entryId);
    }

    protected List<String> retrieveCategories() {
        return repository.getCategories();
    }
}
