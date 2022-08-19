package org.jboss.weld.examples.login;

import java.util.List;

public interface UserManager {

    List<Person> getUsers() throws Exception;

    String addUser() throws Exception;

    Person getNewUser();

    void setNewUser(Person newUser);

}
