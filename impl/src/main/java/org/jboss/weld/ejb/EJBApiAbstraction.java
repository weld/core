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
package org.jboss.weld.ejb;

import java.lang.annotation.Annotation;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ApiAbstraction;

/**
 * Utility class for EJB classes etc. EJB metadata should NOT be inspected here
 * 
 * @author Pete Muir
 */
public class EJBApiAbstraction extends ApiAbstraction implements Service
{
   
   public EJBApiAbstraction(ResourceLoader resourceLoader)
   {
      super(resourceLoader);
      EJB_ANNOTATION_CLASS = annotationTypeForName("javax.ejb.EJB");
      RESOURCE_ANNOTATION_CLASS = annotationTypeForName("javax.annotation.Resource");
      TIMEOUT_ANNOTATION_CLASS = annotationTypeForName("javax.ejb.Timeout");
   }

   public final Class<? extends Annotation> EJB_ANNOTATION_CLASS;
   public final Class<? extends Annotation> RESOURCE_ANNOTATION_CLASS;
   public final Class<? extends Annotation> TIMEOUT_ANNOTATION_CLASS;

   
   public void cleanup() {}
   
}
