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

import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Dan Allen
 */
public
@Named
@RequestScoped
class Comment
{
   private Long id;

   private BlogEntry entry;

   private Date postDate;

   private String author;

   private boolean remember;

   private String body;

   private Users users;

   public Comment()
   {
   }

   public Comment(Long id, BlogEntry entry, String author, Date postDate, String body)
   {
      this.id = id;
      this.entry = entry;
      this.author = author;
      this.postDate = postDate;
      this.body = body;
   }

   public Comment(Comment other)
   {
      this.id = other.getId();
      this.entry = other.getEntry();
      this.author = other.getAuthor();
      this.postDate = other.getPostDate();
      this.body = other.getBody();
   }

   public void checkAuthor()
   {
      if (users != null && isRemember())
         users.setUsername(author);
   }

   public BlogEntry getEntry()
   {
      return entry;
   }

   public void setEntry(BlogEntry entry)
   {
      this.entry = entry;
   }

   public Date getPostDate()
   {
      return postDate;
   }

   public void setPostDate(Date postDate)
   {
      this.postDate = postDate;
   }

   public String getAuthor()
   {
      if (users != null)
      {
         String username = users.getUsername();
         if (username != null)
            return username;
      }
      return author;
   }

   public void setAuthor(String author)
   {
      this.author = author;
   }

   public boolean isRemember()
   {
      return remember;
   }

   public void setRemember(boolean remember)
   {
      this.remember = remember;
   }

   public String getBody()
   {
      return body;
   }

   public void setBody(String body)
   {
      this.body = body;
   }

   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   @Inject
   public void setUsers(Users users)
   {
      this.users = users;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("Comment@").append(hashCode()).append("{");
      sb.append("id=").append(id).append("; ");
      sb.append("author=").append(author).append("; ");
      sb.append("body=").append(body);
      sb.append("}");
      return sb.toString();
   }
}
