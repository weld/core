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
package org.jboss.webbeans.validation.spi;

import javax.validation.ValidatorFactory;

import org.jboss.webbeans.bootstrap.api.Service;

/**
 * <p>
 * Responsible for accessing Bean Validation functionality the environment may
 * provide.
 * </p>
 * 
 * <p>
 * Required in a Java EE environment.
 * </p>
 * 
 * @author pmuir
 *
 */
public interface ValidationServices extends Service
{

   /**
    * Obtain a reference to the default ValidatorFactory
    * @return
    */
   public ValidatorFactory getDefaultValidatorFactory();
   
}
