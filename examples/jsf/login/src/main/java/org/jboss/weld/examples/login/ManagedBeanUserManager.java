package org.jboss.weld.examples.login;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import java.util.List;
import java.util.logging.Logger;

@Named("userManager")
@RequestScoped
public class ManagedBeanUserManager implements UserManager {

    @Inject
    private transient Logger logger;

    @Inject
    private EntityManager userDatabase;

    @Inject
    private UserTransaction utx;

    private Person newUser = new Person();

    @Override
    @SuppressWarnings("unchecked")
    @Produces
    @Named
    @RequestScoped
    public List<Person> getUsers() throws Exception {
        try {
            try {
                utx.begin();
                return userDatabase.createQuery("select u from Person u").getResultList();
            } finally {
                utx.commit();
            }
        } catch (Exception e) {
            utx.rollback();
            throw e;
        }
    }

    @Override
    public String addUser() throws Exception {
        if(newUser.getName().isEmpty() || newUser.getUsername().isEmpty() || newUser.getPassword().isEmpty()){
            return "/users.xhtml?faces-redirect=true";
        }
        try {
            try {
                utx.begin();
                userDatabase.persist(newUser);
                logger.info("Added " + newUser);
                return "/users.xhtml?faces-redirect=true";
            } finally {
                utx.commit();
            }
        } catch (Exception e) {
            utx.rollback();
            throw e;
        }
    }

    @Override
    public Person getNewUser() {
        return newUser;
    }

    @Override
    public void setNewUser(Person newUser) {
        this.newUser = newUser;
    }

}
