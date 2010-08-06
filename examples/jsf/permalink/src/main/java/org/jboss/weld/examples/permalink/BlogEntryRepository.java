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
 * @author Dan Allen
 */
public
@ApplicationScoped
class BlogEntryRepository
{
   private static AtomicLong uniqueEntryId = new AtomicLong(0);

   private static AtomicLong uniqueCommentId = new AtomicLong(0);

   private final List<BlogEntry> entries;

   private DateFormat dateParser;

   public BlogEntryRepository()
   {
      entries = new ArrayList<BlogEntry>();
   }

   @PostConstruct
   public void seed()
   {
      entries.add(new BlogEntry(uniqueEntryId.incrementAndGet(), "Dan", "General", "My first post", parseDate("15/01/09 13:30"), "This is the obligatory first post."));
      entries.add(new BlogEntry(uniqueEntryId.incrementAndGet(), "Dan", "JSF 2", "View parameters", parseDate("16/01/09 15:12"), "One of the new features in JSF 2 is view parameters. If you are familiar with Seam, you will recognize them as page parameters. They bring the value binding concept to GET requests by allowing you to associate a request parameter with a EL value binding expression. You can witness view parameters in action in this demo. View parameters are especially useful for ensuring that the URL is always bookmarkable throughout the user's browsing session."));
      entries.add(new BlogEntry(uniqueEntryId.incrementAndGet(), "Dan", "JSF 2", "Bookmarkable links", parseDate("18/01/09 14:45"), "JSF finally has bookmarkable links. No more complaining that \"everything is a POST!\""));
      entries.add(new BlogEntry(uniqueEntryId.incrementAndGet(), "Dan", "Java EE", "What's new in Java EE 6", parseDate("20/01/09 08:15"), "JSF 2, JPA 2, EJB 3.1, Java Contexts and Dependency Injection (formally Web Beans) and more!"));
      entries.add(new BlogEntry(uniqueEntryId.incrementAndGet(), "Dan", "JSF 2", "Annotation nation", parseDate("22/01/09 10:34"), "You can finally free yourself from the tangles of XML and use annotations to define your managed beans. You put @ManagedBean on a class to define a managed bean and you annotate a field with @ManagedProperty to wire two beans together. If those annotations aren't enough to satisfy you, then check out JSR-299: Java Contexts and Dependency Injection. That spec uses annotations extensively and is a drop-in replacement for the JSF managed bean facility...and a much more capabable one at that!"));
      entries.add(new BlogEntry(uniqueEntryId.incrementAndGet(), "Dan", "JSF 2", "Mojarra == RI", parseDate("25/01/09 11:00"), "The JSF reference implementation finally has a name: Mojarra. Surprise, it's the name of a fish."));
      Collections.sort(entries, new Comparator<BlogEntry>()
      {
         public int compare(BlogEntry a, BlogEntry b)
         {
            return b.getPostDate().compareTo(a.getPostDate());
         }
      });
   }

   public List<BlogEntry> searchEntries(String searchString, int offset, int count)
   {
      if (count == 0)
      {
         return Collections.<BlogEntry>emptyList();
      }

      if (searchString == null || searchString.trim().length() == 0)
      {
         return getLatestEntries(offset, count);
      }

      searchString = searchString.trim().toLowerCase();

      List<BlogEntry> results = new ArrayList<BlogEntry>();
      int idx = 0;
      for (BlogEntry entry : entries)
      {
         if ((entry.getTitle().toLowerCase().contains(searchString) || entry.getBody().toLowerCase().contains(searchString)) &&
            idx++ >= offset)
         {
            results.add(entry);
            if (results.size() == count)
            {
               return results;
            }
         }
      }

      return results;
   }

   public List<BlogEntry> searchEntries(String searchString, String category, int offset, int count)
   {
      if (count == 0)
      {
         return Collections.<BlogEntry>emptyList();
      }

      if (category == null || category.trim().length() == 0)
      {
         return searchEntries(searchString, offset, count);
      }

      if (searchString == null || searchString.trim().length() == 0)
      {
         return getLatestEntries(offset, count);
      }

      category = category.trim();
      searchString = searchString.trim().toLowerCase();

      List<BlogEntry> results = new ArrayList<BlogEntry>();
      int idx = 0;
      for (BlogEntry entry : entries)
      {
         if (entry.getCategory().equals(category) &&
            (entry.getTitle().toLowerCase().contains(searchString) || entry.getBody().toLowerCase().contains(searchString)) &&
            idx++ >= offset)
         {
            results.add(entry);
            if (results.size() == count)
            {
               return results;
            }
         }
      }

      return results;
   }

   public List<BlogEntry> getLatestEntries(String category, int offset, int count)
   {
      if (count == 0)
      {
         return Collections.<BlogEntry>emptyList();
      }

      if (category == null || category.trim().length() == 0)
      {
         return getLatestEntries(offset, count);
      }

      category = category.trim();

      List<BlogEntry> results = new ArrayList<BlogEntry>();
      int idx = 0;
      for (BlogEntry entry : entries)
      {
         if (entry.getCategory().equals(category) && idx++ >= offset)
         {
            results.add(entry);
            if (results.size() == count)
            {
               return results;
            }
         }
      }

      return results;
   }

   public List<BlogEntry> getLatestEntries(int offset, int count)
   {
      if (count == 0)
      {
         return Collections.<BlogEntry>emptyList();
      }

      List<BlogEntry> results = new ArrayList<BlogEntry>();
      int idx = 0;
      for (BlogEntry entry : entries)
      {
         if (idx++ >= offset)
         {
            results.add(entry);
            if (results.size() == count)
            {
               return results;
            }
         }
      }

      return results;
   }

   public BlogEntry getEntry(Long entryId)
   {
      // TODO index the entries by id
      for (BlogEntry entry : entries)
      {
         if (entry.getId().equals(entryId))
         {
            return entry;
         }
      }

      return null;
   }

   public List<String> getCategories()
   {
      // TODO index list of categories
      List<String> categories = new ArrayList<String>();
      for (BlogEntry entry : entries)
      {
         if (!categories.contains(entry.getCategory()))
         {
            categories.add(entry.getCategory());
         }
      }

      Collections.sort(categories);
      return categories;
   }

   public void addComment(Comment comment, Long entryId)
   {
      addComment(comment, getEntry(entryId));
   }

   public void addComment(Comment comment, BlogEntry entry)
   {
      comment.setId(uniqueCommentId.incrementAndGet());
      comment.setPostDate(new Date());
      comment.setEntry(entry);
      synchronized (entry)
      {
         // clone to break reference to the managed bean
         entry.getComments().add(new Comment(comment));
      }
   }

   private Date parseDate(String dateString)
   {
      if (dateParser == null)
      {
         dateParser = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK);
      }

      try
      {
         return dateParser.parse(dateString);
      }
      catch (ParseException e)
      {
         throw new RuntimeException(e);
      }
   }
}
