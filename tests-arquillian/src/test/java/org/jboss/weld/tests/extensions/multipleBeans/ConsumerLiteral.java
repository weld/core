/**
 *
 */
package org.jboss.weld.tests.extensions.multipleBeans;

import javax.enterprise.util.AnnotationLiteral;

class ConsumerLiteral extends AnnotationLiteral<Consumer> implements Consumer {

    final String name;

    ConsumerLiteral(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

}
