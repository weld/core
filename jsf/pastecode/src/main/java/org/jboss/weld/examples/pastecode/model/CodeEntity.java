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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Lob;
import javax.persistence.Transient;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The persistent class for the code database table.
 * 
 */
@Entity
@Table(name = "code")
public class CodeEntity implements Serializable, Cloneable
{
   private static final long MS_PER_SECOND = 1000;
   private static final long MS_PER_MINUTE = 60 * MS_PER_SECOND;
   private static final long MS_PER_HOUR = 60 * MS_PER_MINUTE;
   private static final long MS_PER_DAY = 24 * MS_PER_HOUR;
   
   private static final SimpleDateFormat df = new SimpleDateFormat("d MMM");   
   
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

   public CodeEntity()
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
   
   @Transient
   public String getFriendlyDate()
   {
      if (getDatetime() == null) return "unknown";
      
      Date now = new Date();
      
      long age = now.getTime() - getDatetime().getTime();
      
      long days = (long) Math.floor(age / MS_PER_DAY);
      age -= (days * MS_PER_DAY);
      long hours = (long) Math.floor(age / MS_PER_HOUR);
      age -= (hours * MS_PER_HOUR);
      long minutes = (long) Math.floor(age / MS_PER_MINUTE);
      
      if (days < 7)
      {
         StringBuilder sb = new StringBuilder();
         
         if (days > 0)
         {
            sb.append(days);
            sb.append(days > 1 ? " days " : " day ");
         }
         
         if (hours > 0)
         {
            sb.append(hours);
            sb.append(hours > 1 ? " hrs " : " hr ");
         }
         
         if (minutes > 0)
         {
            sb.append(minutes);
            sb.append(minutes > 1 ? " minutes " : " minute ");
         }
         
         if (hours == 0 && minutes == 0)
         {
            sb.append("just now");
         }
         else
         {         
            sb.append("ago");
         }
         
         return sb.toString();
      }
      else
      {
         return df.format(getDatetime());
      }      
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