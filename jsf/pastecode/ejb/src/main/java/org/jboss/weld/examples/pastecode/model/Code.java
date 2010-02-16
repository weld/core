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

//   public Object clone()
//   {
//      Code c = null;
//      try
//      {
//         c = (Code) super.clone();
//         c.setDatetime(this.datetime);
//         c.setHash(this.hash);
//         c.setId(this.id);
//         c.setLanguage(this.language);
//         c.setNote(this.note);
//         c.setText(this.text);
//         c.setUser(this.user);
//      }
//      catch (CloneNotSupportedException e)
//      {
//         e.printStackTrace();
//      }
//      return c;
//   }

}