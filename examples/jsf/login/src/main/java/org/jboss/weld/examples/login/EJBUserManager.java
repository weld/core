package org.jboss.weld.examples.login;

import java.util.List;
import java.util.logging.Logger;

import jakarta.ejb.Stateful;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

@Named("userManager")
@RequestScoped
@Alternative
@Stateful
public class EJBUserManager implements UserManager {

    @Inject
    private transient Logger logger;

    @Inject
    private EntityManager userDatabase;

    private Person newUser = new Person();

    @SuppressWarnings("unchecked")
    @Produces
    @Named
    @RequestScoped
    public List<Person> getUsers() throws Exception {
        return userDatabase.createQuery("select u from User u").getResultList();
    }

    public String addUser() throws Exception {
        if (newUser.getName().isEmpty() || newUser.getUsername().isEmpty() || newUser.getPassword().isEmpty()) {
            return "/users.xhtml?faces-redirect=true";
        }
        userDatabase.persist(newUser);
        logger.info("Added " + newUser);
        return "/users.xhtml?faces-redirect=true";
    }

    public Person getNewUser() {
        return newUser;
    }

    public void setNewUser(Person newUser) {
        this.newUser = newUser;
    }

}
