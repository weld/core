package org.jboss.weld.examples.login;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
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

    private User newUser = new User();

    @Override
    @SuppressWarnings("unchecked")
    @Produces
    @Named
    @RequestScoped
    public List<User> getUsers() throws Exception {
        try {
            try {
                utx.begin();
                return userDatabase.createQuery("select u from User u").getResultList();
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
    public User getNewUser() {
        return newUser;
    }

    @Override
    public void setNewUser(User newUser) {
        this.newUser = newUser;
    }

}
