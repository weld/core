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
package org.jboss.weld.util.bytecode;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * Utility class for working with constructors in the low level javassist API
 * 
 * @author Stuart Douglas
 * 
 */
public class ConstructorUtils
{

   private ConstructorUtils()
   {
   }

   /**
    * adds a constructor that calls super()
    */
   public static void addDefaultConstructor(ClassFile file, Bytecode initialValueBytecode)
   {
      addConstructor("()V", new String[0], file, initialValueBytecode);
   }

   /**
    * Adds a constructor that delegates to a super constructor with the same
    * descriptor. The bytecode in inialValueBytecode will be executed at the
    * start of the constructor and can be used to inialize fields to a default
    * value. As the object is not properly constructed at this point this
    * bytecode may not reference this (i.e. the variable at location 0)
    * 
    * @param descriptor the constructor descriptor
    * @param exceptions any exceptions that are thrown
    * @param file the classfile to add the constructor to
    * @param initialValueBytecode bytecode that can be used to set inial values
    */
   public static void addConstructor(String descriptor, String[] exceptions, ClassFile file, Bytecode initialValueBytecode)
   {
      try
      {
         MethodInfo ctor = new MethodInfo(file.getConstPool(), "<init>", descriptor);
         ctor.setAccessFlags(AccessFlag.PUBLIC);

         ExceptionsAttribute exAt = new ExceptionsAttribute(file.getConstPool());
         exAt.setExceptions(exceptions);
         ctor.setExceptionsAttribute(exAt);
         Bytecode b = new Bytecode(file.getConstPool());
         String[] params = DescriptorUtils.descriptorStringToParameterArray(descriptor);
         // we need to generate a constructor with a single invokespecial call
         // to the super constructor
         // to do this we need to push all the arguments on the stack first
         // local variables is the number of parameters +1 for this
         // if some of the parameters are wide this may go up.
         b.add(Opcode.ALOAD_0);
         int localVariableCount = BytecodeUtils.loadParameters(b, descriptor);
         // now we have the parameters on the stack
         b.addInvokespecial(file.getSuperclass(), "<init>", descriptor);
         b.addOpcode(Opcode.RETURN);
         CodeAttribute ca = b.toCodeAttribute();
         // set the initial field values
         ca.iterator().insert(initialValueBytecode.get());
         ctor.setCodeAttribute(ca);
         ca.setMaxLocals(localVariableCount);
         ca.computeMaxStack();
         file.addMethod(ctor);
      }
      catch (BadBytecode e)
      {
         throw new RuntimeException(e);
      }
      catch (DuplicateMemberException e)
      {
         throw new RuntimeException(e);
      }
   }
}
