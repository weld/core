/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.introspector.jlr;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.introspector.WBCallable;
import org.jboss.webbeans.introspector.WBClass;

/**
 * @author pmuir
 *
 */
public abstract class AbstractWBCallable<T, X, S extends Member> extends AbstractWBMember<T, X, S> implements WBCallable<T, X, S>
{

   
   
   protected AbstractWBCallable(AnnotationStore annotatedItemHelper, Member member, Class<T> rawType, Type type, WBClass<X> declaringType)
   {
      super(annotatedItemHelper, member, rawType, type, declaringType);
   }

}
