package org.jboss.weld.tests.resolution.circular.resource;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Bar {

    @Id
    @GeneratedValue
    private int id;

}
