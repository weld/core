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

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.desc.FieldDesc;

/**
 * utility class for common bytecode operations
 *
 * @author Stuart Douglas
 */
public class BytecodeUtils {

    public static final String VOID_CLASS_DESCRIPTOR = "V";
    public static final String BYTE_CLASS_DESCRIPTOR = "B";
    public static final String CHAR_CLASS_DESCRIPTOR = "C";
    public static final String DOUBLE_CLASS_DESCRIPTOR = "D";
    public static final String FLOAT_CLASS_DESCRIPTOR = "F";
    public static final String INT_CLASS_DESCRIPTOR = "I";
    public static final String LONG_CLASS_DESCRIPTOR = "J";
    public static final String SHORT_CLASS_DESCRIPTOR = "S";
    public static final String BOOLEAN_CLASS_DESCRIPTOR = "Z";

    private static final String TYPE = "TYPE";
    private static final String LJAVA_LANG_CLASS = "Ljava/lang/Class;";

    public static final int ENUM = 0x00004000;
    public static final int ANNOTATION = 0x00002000;

    private BytecodeUtils() {
    }

    /**
     * NOTE: This method is not applicable in Gizmo 2 where parameters are declared
     * with parameter() and accessed directly as ParamVar instances.
     *
     * @deprecated In Gizmo 2, use parameter() to declare parameters and reference them directly
     */
    @Deprecated
    public static void addLoadInstruction(Object code, String type, int variable) {
        throw new UnsupportedOperationException(
                "addLoadInstruction is not applicable in Gizmo 2 - use parameter() to declare parameters");
    }

    /**
     * Loads a Class object from the string representation.
     * This can handle both object types and primitives.
     *
     * @param b the block creator
     * @param classType the type descriptor for the class or primitive to load.
     *        This will accept both the java.lang.Object form and the
     *        Ljava/lang/Object; form
     * @return the Expr representing the Class object
     */
    public static Expr pushClassType(BlockCreator b, String classType) {
        if (classType.length() != 1) {
            // Object or array type
            if (classType.startsWith("L") && classType.endsWith(";")) {
                classType = classType.substring(1, classType.length() - 1);
            }
            // Convert internal name (slashes) to binary name (dots)
            String className = classType.replace('/', '.');
            // In Gizmo 2, we use Const.of() for class literals
            return Const.of(ClassDesc.of(className));
        } else {
            // Primitive type - load the TYPE field from the wrapper class
            char type = classType.charAt(0);
            Class<?> wrapperClass;
            switch (type) {
                case 'I':
                    wrapperClass = Integer.class;
                    break;
                case 'J':
                    wrapperClass = Long.class;
                    break;
                case 'S':
                    wrapperClass = Short.class;
                    break;
                case 'F':
                    wrapperClass = Float.class;
                    break;
                case 'D':
                    wrapperClass = Double.class;
                    break;
                case 'B':
                    wrapperClass = Byte.class;
                    break;
                case 'C':
                    wrapperClass = Character.class;
                    break;
                case 'Z':
                    wrapperClass = Boolean.class;
                    break;
                default:
                    throw new RuntimeException("Cannot handle primitive type: " + type);
            }
            FieldDesc typeField = FieldDesc.of(wrapperClass, TYPE);
            return b.getStaticField(typeField);
        }
    }

    public static String getName(String descriptor) {
        if (!descriptor.startsWith("[")) {
            return descriptor.substring(1).substring(0, descriptor.length() - 2);
        }
        return descriptor;
    }

}
