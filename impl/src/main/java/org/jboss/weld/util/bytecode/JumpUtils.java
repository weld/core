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

import javassist.bytecode.Bytecode;

/**
 * Utilities for writiting conditional statements in bytecode
 * 
 * @author Stuart Douglas
 * 
 */
public class JumpUtils
{
   /**
    * After writing the instruction that requires a branch offset (e.g. GOTO )
    * to the {@link Bytecode} call this method. This will write two zero bytes
    * to the stream. When you have reached the position in the bytecode that you
    * want the jump to end at call {@link JumpMarker#mark()}, this will update
    * the branch offset to point to the next bytecode instruction that is added
    * 
    * @return The JumpMarker that is used to set the conditionals end point
    */
   public static JumpMarker addJumpInstruction(Bytecode code)
   {
      return new JumpMarkerImpl(code);
   }

   private static class JumpMarkerImpl implements JumpMarker
   {
      private final Bytecode code;
      private int position;

      public JumpMarkerImpl(Bytecode code)
      {
         this.code = code;
         this.position = code.currentPc() - 1;
         code.addIndex(0);
      }

      public void mark()
      {
         code.write16bit(position + 1, code.currentPc() - position);
      }
   }
}
