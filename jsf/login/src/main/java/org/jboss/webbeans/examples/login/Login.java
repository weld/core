package org.jboss.webbeans.examples.login;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@SessionScoped @Named
public class Login implements Serializable {

    @Inject Credentials credentials;
    //@PersistenceContext EntityManager userDatabase;

    private User user;
    
    public void login() {
    	
       List<User> results = /*userDatabase.createQuery(
          "select u from User u where u.username=:username and u.password=:password")
          .setParameter("username", credentials.getUsername())
          .setParameter("password", credentials.getPassword())
          .getResultList();*/
        	
          Arrays.asList( new User(credentials.getUsername(), "Your Name", credentials.getPassword()) );
        
       if ( !results.isEmpty() ) {
          user = results.get(0);
          FacesContext.getCurrentInstance()
             .addMessage(null, new FacesMessage("Welcome, " + user.getName()));
       }
        
    }
    
    public void logout() {
       FacesContext.getCurrentInstance()
          .addMessage(null, new FacesMessage("Goodbye, " + user.getName()));
       user = null;
    }
    
    public boolean isLoggedIn() {
       return user!=null;
    }
    
    @Produces @LoggedIn 
    public User getCurrentUser() {
        return user;
    }

}