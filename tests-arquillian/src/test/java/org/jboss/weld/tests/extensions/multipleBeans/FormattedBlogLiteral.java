/**
 *
 */
package org.jboss.weld.tests.extensions.multipleBeans;

import javax.enterprise.util.AnnotationLiteral;

class FormattedBlogLiteral extends AnnotationLiteral<FormattedBlog> implements FormattedBlog {

    final String name;

    FormattedBlogLiteral(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

}
