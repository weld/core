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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * @author Ales Justin
 */
public interface Repository
{
   void seed();

   List<BlogEntry> searchEntries(String searchString, int offset, int count);

   List<BlogEntry> searchEntries(String searchString, String category, int offset, int count);

   List<BlogEntry> getLatestEntries(String category, int offset, int count);

   List<BlogEntry> getLatestEntries(int offset, int count);

   BlogEntry getEntry(Long entryId);

   List<String> getCategories();

   void addComment(Comment comment, Long entryId);

   void addComment(Comment comment, BlogEntry entry);
}
