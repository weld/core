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
package org.jboss.weld.examples.permalink;

import java.util.List;

/**
 * @author Ales Justin
 */
public interface Repository {
    List<BlogEntry> searchEntries(String searchString, int offset, int count);

    List<BlogEntry> searchEntries(String searchString, String category, int offset, int count);

    List<BlogEntry> getLatestEntries(String category, int offset, int count);

    List<BlogEntry> getLatestEntries(int offset, int count);

    BlogEntry getEntry(Long entryId);

    List<String> getCategories();

    void addComment(Comment comment, Long entryId);

    void addComment(Comment comment, BlogEntry entry);
}
