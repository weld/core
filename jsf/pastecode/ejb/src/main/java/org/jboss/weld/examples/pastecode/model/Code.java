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
package org.jboss.weld.examples.pastecode.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;

/**
 * The persistent class for the code database table.
 * 
 */
@Entity
@Table(name = "code")
public class Code implements Serializable, Cloneable
{
   private static final long serialVersionUID = 1L;
   private int id;
   private Date datetime;
   private String language;
   private String note;
   private String text;
   private String user;
   private String hash;

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   @Column(name = "hash")
   public String getHash()
   {
      return hash;
   }

   public void setHash(String hash)
   {
      this.hash = hash;
   }

   public Code()
   {
      this.language = "";
      this.note = "";
      this.text = "";
      this.user = "";
      this.hash = null;
      this.datetime = null;
   }

   @Temporal(TemporalType.TIMESTAMP)
   @Column(name = "datetime")
   public Date getDatetime()
   {
      return this.datetime;
   }

   public void setDatetime(Date datetime)
   {
      this.datetime = datetime;
   }

   @Column(name = "language")
   public String getLanguage()
   {
      return this.language;
   }

   public void setLanguage(String language)
   {
      this.language = language;
   }

   @Lob()
   @Column(name = "note")
   public String getNote()
   {
      return this.note;
   }

   public void setNote(String note)
   {
      this.note = note;
   }

   @Lob()
   @Column(name = "text")
   public String getText()
   {
      return this.text;
   }

   public void setText(String text)
   {
      this.text = text;
   }

   @Column(name = "user")
   public String getUser()
   {
      return this.user;
   }

   public void setUser(String user)
   {
      this.user = user;
   }
}