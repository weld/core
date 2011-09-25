/**
 *
 */
package org.jboss.weld.tests.extensions.multipleBeans;

import javax.enterprise.util.AnnotationLiteral;

class AuthorLiteral extends AnnotationLiteral<Author> implements Author {

    final String name;

    AuthorLiteral(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

}
