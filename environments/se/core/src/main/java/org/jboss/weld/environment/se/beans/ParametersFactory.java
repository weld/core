/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.weld.environment.se.StartMain;
import org.jboss.weld.environment.se.bindings.Parameters;

/**
 * The simple bean that will hold the command line arguments and make them
 * available by injection (using the @Parameters binding). It's initialised by
 * the StartMain class before your main app is initialised.
 * 
 * @author Peter Royle
 */
@ApplicationScoped
public class ParametersFactory
{
   private String[] args;
   private List<String> argsList;

   public ParametersFactory()
   {
      this.setArgs(StartMain.PARAMETERS);
   }

   /**
    * Producer method for the injectable command line args.
    * 
    * @return The command line arguments.
    */
   @Produces
   @Parameters
   public List<String> getArgs()
   {
      return argsList;
   }

   /**
    * Producer method for the injectable command line args.
    * 
    * @return The command line arguments.
    */
   @Produces
   @Parameters
   public String[] getArgsAsArray()
   {
      return this.args;
   }

   /**
    * StartMain passes in the command line args here.
    * 
    * @param args The command line arguments. If null is given then an empty
    *           array will be used instead.
    */
   public void setArgs(String[] args)
   {
      if (args == null)
      {
         args = new String[] {};
      }
      this.args = args;
      this.argsList = Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(args)));
   }

}
