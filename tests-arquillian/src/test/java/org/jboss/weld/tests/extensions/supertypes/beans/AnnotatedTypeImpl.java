/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.jboss.weld.tests.extensions.supertypes.beans;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Implements the CDI AnnotatedType interface.
 *
 * @author robc
 */
public class AnnotatedTypeImpl<T> extends AnnotatedImpl implements AnnotatedType<T> {

    private Set<AnnotatedConstructor<T>> constructors;
    private Set<AnnotatedField<? super T>> fields;
    private Class<T> javaClass;
    private Set<AnnotatedMethod<? super T>> methods;

    public AnnotatedTypeImpl(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, Class<T> javaClass) {
        super(baseType, typeClosure, annotations);
        this.javaClass = javaClass;
    }

    public AnnotatedTypeImpl(AnnotatedType<T> type) {
        this(type.getBaseType(), type.getTypeClosure(), type.getAnnotations(), type.getJavaClass());
    }

    public Set<AnnotatedConstructor<T>> getConstructors() {
        return constructors;
    }

    public void setConstructors(Set<AnnotatedConstructor<T>> constructors) {
        this.constructors = constructors;
    }

    public Set<AnnotatedField<? super T>> getFields() {
        return fields;
    }

    public void setFields(Set<AnnotatedField<? super T>> fields) {
        this.fields = fields;
    }

    public Class<T> getJavaClass() {
        return javaClass;
    }

    public Set<AnnotatedMethod<? super T>> getMethods() {
        return methods;
    }

    public void setMethods(Set<AnnotatedMethod<? super T>> methods) {
        this.methods = methods;
    }
}
