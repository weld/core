package org.jboss.weld.examples.login;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User
{
   @Id
   private String username;
   private String name;
   @SuppressWarnings("unused")
   private String password;
   
   public User() {}

   public String getUsername()
   {
      return username;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getName()
   {
      return name;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }
   
   public String getPassword()
   {
      return password;
   }

}
