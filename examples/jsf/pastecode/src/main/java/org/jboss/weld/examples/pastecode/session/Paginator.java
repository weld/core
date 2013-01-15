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

import java.util.ArrayList;
import java.util.List;

/**
 * Pagination support class
 *
 * @author Martin Gencur
 */
public class Paginator {

    private int recordsCount = 0;
    private int pagesCount = 0;
    private int numLinks = 8;
    private int startIndex;
    private int endIndex;
    private int page = 0;
    private List<Integer> indexes;

    public int getNumLinks() {
        return numLinks;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;

        if (pagesCount == 1) {
            this.setBoundedIndexes(0, 0);
        }

        if (this.page > (numLinks / 2)) {
            this.setStartIndex(this.page - (numLinks / 2));
        } else {
            this.setStartIndex(0);
            this.setEndIndex((numLinks > this.pagesCount) ? this.pagesCount : numLinks);
        }

        if (this.page + (numLinks / 2) >= this.pagesCount) {
            this.setEndIndex(this.pagesCount);
            this.setStartIndex((this.pagesCount - numLinks) < 0 ? 0 : this.pagesCount - numLinks);
        } else {
            if (this.page < (numLinks / 2)) {
                this.setEndIndex((numLinks > this.pagesCount) ? this.pagesCount : numLinks);
            } else {
                this.setEndIndex(this.page + (numLinks / 2));
            }

        }
        this.setBoundedIndexes(this.startIndex, this.endIndex);
    }

    public void setBoundedIndexes(int startIndex, int endIndex) {
        this.indexes = new ArrayList<Integer>(endIndex - startIndex);
        for (int i = startIndex; i < endIndex; i++) {
            this.indexes.add(new Integer(i));
        }
    }

    public List<Integer> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Integer> indexes) {
        this.indexes = indexes;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
}
