/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.proxy;

import static org.jboss.weld.util.bytecode.DescriptorUtils.BOOLEAN_CLASS_DESCRIPTOR;
import static org.jboss.weld.util.bytecode.DescriptorUtils.DOUBLE_CLASS_DESCRIPTOR;
import static org.jboss.weld.util.bytecode.DescriptorUtils.LONG_CLASS_DESCRIPTOR;
import static org.jboss.weld.util.bytecode.DescriptorUtils.VOID_CLASS_DESCRIPTOR;
import static org.jboss.weld.util.bytecode.DescriptorUtils.classToStringRepresentation;
import static org.jboss.weld.util.bytecode.DescriptorUtils.getMethodDescriptor;

import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.code.ExceptionHandler;

/**
 * Generates bytecode that wraps {@link #doWork(CodeAttribute, ClassMethod)} within {@link InterceptionDecorationContext#startInterceptorContextIfNotEmpty()}
 * and {@link InterceptionDecorationContext#endInterceptorContext()}
 *
 * @author Stuart Douglas
 * @author Jozef Hartinger
 *
 */
abstract class RunWithinInterceptionDecorationContextGenerator {

    static final String INTERCEPTION_DECORATION_CONTEXT_CLASS_NAME = InterceptionDecorationContext.class.getName();
    static final String START_INTERCEPTOR_CONTEXT_IF_NOT_EMPTY_METHOD_NAME = "startIfNotEmpty";
    static final String START_INTERCEPTOR_CONTEXT_IF_NOT_ON_TOP_METHOD_NAME = "startIfNotOnTop";
    static final String START_INTERCEPTOR_CONTEXT_IF_NOT_ON_TOP_METHOD_SIGNATURE = getMethodDescriptor(
            new String[] { classToStringRepresentation(CombinedInterceptorAndDecoratorStackMethodHandler.class) }, BOOLEAN_CLASS_DESCRIPTOR);
    static final String END_INTERCEPTOR_CONTEXT_METHOD_NAME = "endInterceptorContext";
    private static final String EMPTY_PARENTHESES = "()";

    private final ClassMethod classMethod;
    private final CodeAttribute b;

    RunWithinInterceptionDecorationContextGenerator(ClassMethod classMethod) {
        this.classMethod = classMethod;
        this.b = classMethod.getCodeAttribute();
    }

    abstract void doWork(CodeAttribute b, ClassMethod method);
    abstract void doReturn(CodeAttribute b, ClassMethod method);

    void startIfNotEmpty(CodeAttribute b, ClassMethod method) {
        b.invokestatic(INTERCEPTION_DECORATION_CONTEXT_CLASS_NAME, START_INTERCEPTOR_CONTEXT_IF_NOT_EMPTY_METHOD_NAME, EMPTY_PARENTHESES
                + BOOLEAN_CLASS_DESCRIPTOR);
        // store the outcome some that we know later whether to end the context or not
        storeToLocalVariable(0);
    }

    void startIfNotOnTop(CodeAttribute b, ClassMethod method) {
        b.aload(0);
        b.getfield(method.getClassFile().getName(), ProxyFactory.METHOD_HANDLER_FIELD_NAME, classToStringRepresentation(MethodHandler.class));
        b.dup();

        // if handler != null (may happen inside constructor calls)
        final BranchEnd handlerNull = b.ifnull();
        b.checkcast(CombinedInterceptorAndDecoratorStackMethodHandler.class.getName());
        b.invokestatic(INTERCEPTION_DECORATION_CONTEXT_CLASS_NAME, START_INTERCEPTOR_CONTEXT_IF_NOT_ON_TOP_METHOD_NAME,
                START_INTERCEPTOR_CONTEXT_IF_NOT_ON_TOP_METHOD_SIGNATURE);
        final BranchEnd endOfIfStatement = b.gotoInstruction();
        b.branchEnd(handlerNull);
        // else started = false
        b.pop(); // pop null value out of the stack
        b.iconst(0);
        b.branchEnd(endOfIfStatement);

        storeToLocalVariable(0);
    }

    void withinCatchBlock(CodeAttribute b, ClassMethod method) {
        final ExceptionHandler start = b.exceptionBlockStart(Throwable.class.getName());

        doWork(b, method);

        // end the interceptor context, everything was fine
        endIfStarted(b, method);


        // jump over the catch block
        BranchEnd gotoEnd = b.gotoInstruction();

        // create catch block
        b.exceptionBlockEnd(start);
        b.exceptionHandlerStart(start);

        // end the interceptor context if there was an exception
        endIfStarted(b, method);
        b.athrow();

        // update the correct address to jump over the catch block
        b.branchEnd(gotoEnd);

        doReturn(b, method);
    }

    /**
     * Ends interception context if it was previously stated. This is indicated by a local variable with index 0.
     */
    void endIfStarted(CodeAttribute b, ClassMethod method) {
        b.iload(getLocalVariableIndex(0));
        final BranchEnd conditional = b.ifeq();
        b.invokestatic(INTERCEPTION_DECORATION_CONTEXT_CLASS_NAME, END_INTERCEPTOR_CONTEXT_METHOD_NAME, EMPTY_PARENTHESES + VOID_CLASS_DESCRIPTOR);
        b.branchEnd(conditional);
    }

    /**
     * Generates bytecode that invokes {@link InterceptionDecorationContext#startIfNotEmpty()} and stores the result in a local variable. Then, the bytecode
     * generated by {@link #doWork(CodeAttribute, ClassMethod)} is added. Lastly, bytecode that conditionally calls {@link InterceptionDecorationContext} based
     * on the value of the local variable is added. This is done within a catch block so that the context is ended no matter if the bytecode generated by
     * {@link #doWork(CodeAttribute, ClassMethod)} yields an exception or not.
     */
    void runStartIfNotEmpty() {
        startIfNotEmpty(b, classMethod);
        withinCatchBlock(b, classMethod);
    }

    /**
     * Generates bytecode that loads the "methodHandler" field, invokes
     * {@link InterceptionDecorationContext#startIfNotOnTop(CombinedInterceptorAndDecoratorStackMethodHandler)} and stores the result in a local variable. Then,
     * the bytecode generated by {@link #doWork(CodeAttribute, ClassMethod)} is added. Lastly, bytecode that conditionally calls
     * {@link InterceptionDecorationContext} based on the value of the local variable is added. This is done within a catch block so that the context is ended
     * no matter if the bytecode generated by {@link #doWork(CodeAttribute, ClassMethod)} yields an exception or not.
     */
    void runStartIfNotOnTop() {
        startIfNotOnTop(b, classMethod);
        withinCatchBlock(b, classMethod);
    }

    void storeToLocalVariable(int i) {
        b.istore(getLocalVariableIndex(0));
    }

    /**
     * Gets the index of a local variable (the first index after method parameters). Indexes start with 0.
     */
    private int getLocalVariableIndex(int i) {
        int index = classMethod.isStatic() ? 0 : 1;
        for (String type : classMethod.getParameters()) {
            if (type.equals(DOUBLE_CLASS_DESCRIPTOR) || type.equals(LONG_CLASS_DESCRIPTOR)) {
                index += 2;
            } else {
                index++;
            }
        }
        return index + i;
    }
}
