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

import org.jboss.classfilewriter.code.CodeAttribute;

/**
 * This class is responsible for generating CodeAttribute fragments to box/unbox
 * whatever happens to be on the top of the stack.
 * <p/>
 * It is the calling codes responsibility to make sure that the correct type is
 * on the stack
 *
 * @author Stuart Douglas
 */
public class Boxing {

    private static final String JAVA_LANG_NUMBER = Number.class.getName();
    private static final String JAVA_LANG_CHARACTER = Character.class.getName();
    private static final String JAVA_LANG_BOOLEAN = Boolean.class.getName();
    private static final String VALUE_OF = "valueOf";

    private Boxing() {
    }

    public static void boxIfNecessary(CodeAttribute b, String desc) {
        if (desc.length() == 1) {
            char type = desc.charAt(0);
            switch (type) {
                case 'I':
                    boxInt(b);
                    break;
                case 'J':
                    boxLong(b);
                    break;
                case 'S':
                    boxShort(b);
                    break;
                case 'F':
                    boxFloat(b);
                    break;
                case 'D':
                    boxDouble(b);
                    break;
                case 'B':
                    boxByte(b);
                    break;
                case 'C':
                    boxChar(b);
                    break;
                case 'Z':
                    boxBoolean(b);
                    break;
                default:
                    throw new RuntimeException("Cannot box unknown primitive type: " + type);
            }
        }
    }

    public static CodeAttribute unbox(CodeAttribute b, String desc) {
        char type = desc.charAt(0);
        switch (type) {
            case 'I':
                return unboxInt(b);
            case 'J':
                return unboxLong(b);
            case 'S':
                return unboxShort(b);
            case 'F':
                return unboxFloat(b);
            case 'D':
                return unboxDouble(b);
            case 'B':
                return unboxByte(b);
            case 'C':
                return unboxChar(b);
            case 'Z':
                return unboxBoolean(b);
            default:
                throw new RuntimeException("Cannot unbox unknown primitive type: " + type);
        }
    }

    public static void boxInt(CodeAttribute bc) {
        bc.invokestatic("java.lang.Integer", VALUE_OF, "(I)Ljava/lang/Integer;");
    }

    public static void boxLong(CodeAttribute bc) {
        bc.invokestatic("java.lang.Long", VALUE_OF, "(J)Ljava/lang/Long;");
    }

    public static void boxShort(CodeAttribute bc) {
        bc.invokestatic("java.lang.Short", VALUE_OF, "(S)Ljava/lang/Short;");
    }

    public static void boxByte(CodeAttribute bc) {
        bc.invokestatic("java.lang.Byte", VALUE_OF, "(B)Ljava/lang/Byte;");
    }

    public static void boxFloat(CodeAttribute bc) {
        bc.invokestatic("java.lang.Float", VALUE_OF, "(F)Ljava/lang/Float;");
    }

    public static void boxDouble(CodeAttribute bc) {
        bc.invokestatic("java.lang.Double", VALUE_OF, "(D)Ljava/lang/Double;");
    }

    public static void boxChar(CodeAttribute bc) {
        bc.invokestatic(JAVA_LANG_CHARACTER, VALUE_OF, "(C)Ljava/lang/Character;");
    }

    public static void boxBoolean(CodeAttribute bc) {
        bc.invokestatic(JAVA_LANG_BOOLEAN, VALUE_OF, "(Z)Ljava/lang/Boolean;");
    }

    // unboxing

    public static CodeAttribute unboxInt(CodeAttribute bc) {
        bc.checkcast(JAVA_LANG_NUMBER);
        bc.invokevirtual(JAVA_LANG_NUMBER, "intValue", "()I");
        return bc;
    }

    public static CodeAttribute unboxLong(CodeAttribute bc) {
        bc.checkcast(JAVA_LANG_NUMBER);
        bc.invokevirtual(JAVA_LANG_NUMBER, "longValue", "()J");
        return bc;
    }

    public static CodeAttribute unboxShort(CodeAttribute bc) {
        bc.checkcast(JAVA_LANG_NUMBER);
        bc.invokevirtual(JAVA_LANG_NUMBER, "shortValue", "()S");
        return bc;
    }

    public static CodeAttribute unboxByte(CodeAttribute bc) {
        bc.checkcast(JAVA_LANG_NUMBER);
        bc.invokevirtual(JAVA_LANG_NUMBER, "byteValue", "()B");
        return bc;
    }

    public static CodeAttribute unboxFloat(CodeAttribute bc) {
        bc.checkcast(JAVA_LANG_NUMBER);
        bc.invokevirtual(JAVA_LANG_NUMBER, "floatValue", "()F");
        return bc;
    }

    public static CodeAttribute unboxDouble(CodeAttribute bc) {
        bc.checkcast(JAVA_LANG_NUMBER);
        bc.invokevirtual(JAVA_LANG_NUMBER, "doubleValue", "()D");
        return bc;
    }

    public static CodeAttribute unboxChar(CodeAttribute bc) {
        bc.checkcast(JAVA_LANG_CHARACTER);
        bc.invokevirtual(JAVA_LANG_CHARACTER, "charValue", "()C");
        return bc;
    }

    public static CodeAttribute unboxBoolean(CodeAttribute bc) {
        bc.checkcast(JAVA_LANG_BOOLEAN);
        bc.invokevirtual(JAVA_LANG_BOOLEAN, "booleanValue", "()Z");
        return bc;
    }

}
