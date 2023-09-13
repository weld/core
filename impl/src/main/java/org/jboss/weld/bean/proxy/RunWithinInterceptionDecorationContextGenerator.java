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

import static org.jboss.classfilewriter.util.DescriptorUtils.makeDescriptor;
import static org.jboss.classfilewriter.util.DescriptorUtils.methodDescriptor;
import static org.jboss.weld.util.bytecode.BytecodeUtils.DOUBLE_CLASS_DESCRIPTOR;
import static org.jboss.weld.util.bytecode.BytecodeUtils.LONG_CLASS_DESCRIPTOR;
import static org.jboss.weld.util.bytecode.BytecodeUtils.VOID_CLASS_DESCRIPTOR;

import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.code.ExceptionHandler;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;

/**
 * Generates bytecode that wraps {@link #doWork(CodeAttribute, ClassMethod)} within
 * {@link InterceptionDecorationContext#startInterceptorContextIfNotEmpty()}
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
    static final String END_INTERCEPTOR_CONTEXT_METHOD_NAME = "end";
    private static final String STACK_DESCRIPTOR = makeDescriptor(Stack.class);
    private static final String EMPTY_PARENTHESES = "()";
    private static final String RETURNS_STACK_DESCRIPTOR = EMPTY_PARENTHESES + STACK_DESCRIPTOR;
    static final String START_INTERCEPTOR_CONTEXT_IF_NOT_ON_TOP_METHOD_SIGNATURE = methodDescriptor(
            new String[] { makeDescriptor(CombinedInterceptorAndDecoratorStackMethodHandler.class) }, STACK_DESCRIPTOR);

    private final ClassMethod classMethod;
    private final CodeAttribute b;
    private final ProxyFactory<?> factory;

    RunWithinInterceptionDecorationContextGenerator(ClassMethod classMethod, ProxyFactory<?> factory) {
        this.classMethod = classMethod;
        this.b = classMethod.getCodeAttribute();
        this.factory = factory;
    }

    abstract void doWork(CodeAttribute b, ClassMethod method);

    abstract void doReturn(CodeAttribute b, ClassMethod method);

    void startIfNotEmpty(CodeAttribute b, ClassMethod method) {
        b.invokestatic(INTERCEPTION_DECORATION_CONTEXT_CLASS_NAME, START_INTERCEPTOR_CONTEXT_IF_NOT_EMPTY_METHOD_NAME,
                RETURNS_STACK_DESCRIPTOR);
        // store the outcome so that we know later whether to end the context or not
        storeToLocalVariable(0);
    }

    void startIfNotOnTop(CodeAttribute b, ClassMethod method) {
        b.aload(0);
        factory.getMethodHandlerField(method.getClassFile(), b);
        b.dup();

        // if handler != null (may happen inside constructor calls)
        final BranchEnd handlerNull = b.ifnull();
        b.invokestatic(INTERCEPTION_DECORATION_CONTEXT_CLASS_NAME, START_INTERCEPTOR_CONTEXT_IF_NOT_ON_TOP_METHOD_NAME,
                START_INTERCEPTOR_CONTEXT_IF_NOT_ON_TOP_METHOD_SIGNATURE);
        final BranchEnd endOfIfStatement = b.gotoInstruction();
        b.branchEnd(handlerNull);
        // else started = false
        // keeping null handler on top of stack
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
        b.aload(getLocalVariableIndex(0));
        b.dup();
        final BranchEnd ifnotnull = b.ifnull();
        b.checkcast(Stack.class);
        b.invokevirtual(Stack.class.getName(), END_INTERCEPTOR_CONTEXT_METHOD_NAME, EMPTY_PARENTHESES + VOID_CLASS_DESCRIPTOR);
        BranchEnd ifnull = b.gotoInstruction();
        b.branchEnd(ifnotnull);
        b.pop(); // remove null Stack
        b.branchEnd(ifnull);
    }

    /**
     * Generates bytecode that invokes {@link InterceptionDecorationContext#startIfNotEmpty()} and stores the result in a local
     * variable. Then, the bytecode
     * generated by {@link #doWork(CodeAttribute, ClassMethod)} is added. Lastly, bytecode that conditionally calls
     * {@link InterceptionDecorationContext} based
     * on the value of the local variable is added. This is done within a catch block so that the context is ended no matter if
     * the bytecode generated by
     * {@link #doWork(CodeAttribute, ClassMethod)} yields an exception or not.
     */
    void runStartIfNotEmpty() {
        startIfNotEmpty(b, classMethod);
        withinCatchBlock(b, classMethod);
    }

    /**
     * Generates bytecode that loads the "methodHandler" field, invokes
     * {@link InterceptionDecorationContext#startIfNotOnTop(CombinedInterceptorAndDecoratorStackMethodHandler)} and stores the
     * result in a local variable. Then,
     * the bytecode generated by {@link #doWork(CodeAttribute, ClassMethod)} is added. Lastly, bytecode that conditionally calls
     * {@link InterceptionDecorationContext} based on the value of the local variable is added. This is done within a catch
     * block so that the context is ended
     * no matter if the bytecode generated by {@link #doWork(CodeAttribute, ClassMethod)} yields an exception or not.
     */
    void runStartIfNotOnTop() {
        startIfNotOnTop(b, classMethod);
        withinCatchBlock(b, classMethod);
    }

    void storeToLocalVariable(int i) {
        b.astore(getLocalVariableIndex(0));
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
