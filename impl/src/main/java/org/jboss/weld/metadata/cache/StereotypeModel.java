/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.metadata.cache;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static org.jboss.weld.logging.Category.REFLECTION;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.MetadataMessage.MULTIPLE_SCOPES;
import static org.jboss.weld.logging.messages.MetadataMessage.QUALIFIER_ON_STEREOTYPE;
import static org.jboss.weld.logging.messages.MetadataMessage.VALUE_ON_NAMED_STEREOTYPE;
import static org.jboss.weld.logging.messages.ReflectionMessage.MISSING_TARGET;
import static org.jboss.weld.logging.messages.ReflectionMessage.MISSING_TARGET_METHOD_FIELD_TYPE_PARAMETER_OR_TARGET_METHOD_TYPE_OR_TARGET_METHOD_OR_TARGET_TYPE_OR_TARGET_FIELD;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.interceptor.InterceptorBinding;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.util.collections.Arrays2;
import org.slf4j.cal10n.LocLogger;

/**
 * A meta model for a stereotype, allows us to cache a stereotype and to
 * validate it
 *
 * @author Pete Muir
 */
public class StereotypeModel<T extends Annotation> extends AnnotationModel<T> {
    private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Collections.<Class<? extends Annotation>>singleton(Stereotype.class);
    private static final LocLogger log = loggerFactory().getLogger(REFLECTION);

    // Is the stereotype an alternative
    private boolean alternative;
    // The default scope type
    private Annotation defaultScopeType;
    // Is the bean name defaulted
    private boolean beanNameDefaulted;
    // The interceptor bindings
    private Set<Annotation> interceptorBindings;

    private Set<Annotation> inheritedSterotypes;

    private Set<Annotation> metaAnnotations;

    /**
     * Constructor
     *
     * @param sterotype The stereotype
     */
    public StereotypeModel(EnhancedAnnotation<T> enhancedAnnotatedAnnotation) {
        super(enhancedAnnotatedAnnotation);
    }

    @Override
    protected void init(EnhancedAnnotation<T> annotatedAnnotation) {
        super.init(annotatedAnnotation);
        initAlternative(annotatedAnnotation);
        initDefaultScopeType(annotatedAnnotation);
        initBeanNameDefaulted(annotatedAnnotation);
        initInterceptorBindings(annotatedAnnotation);
        initInheritedStereotypes(annotatedAnnotation);
        checkBindings(annotatedAnnotation);
        this.metaAnnotations = annotatedAnnotation.getAnnotations();
    }



    /**
     * Validates the binding types
     */
    private void checkBindings(EnhancedAnnotation<T> annotatedAnnotation) {
        Set<Annotation> bindings = annotatedAnnotation.getMetaAnnotations(Qualifier.class);
        if (bindings.size() > 0) {
            for (Annotation annotation : bindings) {
                if (!annotation.annotationType().equals(Named.class)) {
                    throw new DefinitionException(QUALIFIER_ON_STEREOTYPE, annotatedAnnotation);
                }
            }
        }
    }

    /**
     * Initializes the interceptor bindings
     */
    private void initInterceptorBindings(EnhancedAnnotation<T> annotatedAnnotation) {
        interceptorBindings = annotatedAnnotation.getMetaAnnotations(InterceptorBinding.class);
    }

    private void initInheritedStereotypes(EnhancedAnnotation<T> annotatedAnnotation) {
        this.inheritedSterotypes = annotatedAnnotation.getMetaAnnotations(Stereotype.class);
    }

    /**
     * Initializes the bean name defaulted
     */
    private void initBeanNameDefaulted(EnhancedAnnotation<T> annotatedAnnotation) {
        if (annotatedAnnotation.isAnnotationPresent(Named.class)) {
            if (!"".equals(annotatedAnnotation.getAnnotation(Named.class).value())) {
                throw new DefinitionException(VALUE_ON_NAMED_STEREOTYPE, annotatedAnnotation);
            }
            beanNameDefaulted = true;
        }
    }

    /**
     * Initializes the default scope type
     */
    private void initDefaultScopeType(EnhancedAnnotation<T> annotatedAnnotation) {
        Set<Annotation> scopeTypes = new HashSet<Annotation>();
        scopeTypes.addAll(annotatedAnnotation.getMetaAnnotations(Scope.class));
        scopeTypes.addAll(annotatedAnnotation.getMetaAnnotations(NormalScope.class));
        if (scopeTypes.size() > 1) {
            throw new DefinitionException(MULTIPLE_SCOPES, annotatedAnnotation);
        } else if (scopeTypes.size() == 1) {
            this.defaultScopeType = scopeTypes.iterator().next();
        }
    }

    /**
     * Initializes the default deployment type
     */
    private void initAlternative(EnhancedAnnotation<T> annotatedAnnotation) {
        if (annotatedAnnotation.isAnnotationPresent(Alternative.class)) {
            this.alternative = true;
        }
    }

    @Override
    protected void check(EnhancedAnnotation<T> annotatedAnnotation) {
        super.check(annotatedAnnotation);
        if (isValid()) {
            if (!annotatedAnnotation.isAnnotationPresent(Target.class)) {
                log.debug(MISSING_TARGET, annotatedAnnotation);
            } else if (!(
                    Arrays2.unorderedEquals(annotatedAnnotation.getAnnotation(Target.class).value(), METHOD, FIELD, TYPE) ||
                            Arrays2.unorderedEquals(annotatedAnnotation.getAnnotation(Target.class).value(), TYPE) ||
                            Arrays2.unorderedEquals(annotatedAnnotation.getAnnotation(Target.class).value(), METHOD) ||
                            Arrays2.unorderedEquals(annotatedAnnotation.getAnnotation(Target.class).value(), FIELD) ||
                            Arrays2.unorderedEquals(annotatedAnnotation.getAnnotation(Target.class).value(), METHOD, TYPE)
            )) {
                log.debug(MISSING_TARGET_METHOD_FIELD_TYPE_PARAMETER_OR_TARGET_METHOD_TYPE_OR_TARGET_METHOD_OR_TARGET_TYPE_OR_TARGET_FIELD, annotatedAnnotation);
            }
        }
    }


    /**
     * Get the default scope type the stereotype specifies
     *
     * @return The default scope type, or null if none is specified
     */
    public Annotation getDefaultScopeType() {
        return defaultScopeType;
    }

    /**
     * Get any interceptor bindings the the stereotype specifies
     *
     * @return The interceptor bindings, or an empty set if none are specified.
     */
    public Set<Annotation> getInterceptorBindings() {
        return interceptorBindings;
    }

    /**
     * Indicates if the bean name is defaulted
     *
     * @return True if defaulted, false otherwise
     */
    public boolean isBeanNameDefaulted() {
        return beanNameDefaulted;
    }

    /**
     * Gets the meta-annotation type
     *
     * @return The Stereotype class
     */
    @Override
    protected Set<Class<? extends Annotation>> getMetaAnnotationTypes() {
        return META_ANNOTATIONS;
    }

    /**
     * @return
     */
    public boolean isAlternative() {
        return alternative;
    }

    public Set<Annotation> getInheritedSterotypes() {
        return inheritedSterotypes;
    }

    /**
     * @return the metaAnnotations
     */
    public Set<Annotation> getMetaAnnotations() {
        return metaAnnotations;
    }

}
