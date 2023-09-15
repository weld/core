package org.jboss.weld.examples.login;

import java.io.Serializable;
import java.util.List;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SessionScoped
@Named
public class Login implements Serializable {

    private static final long serialVersionUID = 7965455427888195913L;

    @Inject
    private Credentials credentials;

    @PersistenceContext
    private EntityManager userDatabase;

    private Person currentUser;

    @SuppressWarnings("unchecked")
    public void login() {

        List<Person> results = userDatabase
                .createQuery("select u from Person u where u.username=:username and u.password=:password")
                .setParameter("username", credentials.getUsername()).setParameter("password", credentials.getPassword())
                .getResultList();

        if (!results.isEmpty()) {
            currentUser = results.get(0);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Welcome, " + currentUser.getName()));
        }

    }

    public void logout() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Goodbye, " + currentUser.getName()));
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    @Produces
    @LoggedIn
    public Person getCurrentUser() {
        return currentUser;
    }

}
