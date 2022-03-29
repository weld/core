/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.resolution.circular.self.modifiers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SelfInjectingImpl implements SomeInterface {

    @Inject
    SelfInjectingImpl self;

    @Override
    public Integer selfInvokePublicMethodPublicParamsPublicReturnType(Integer foo) {
        return self._selfInvokePublicMethodPublicParamsPublicReturnType(foo);
    }

    @Override
    public Integer selfInvokePublicMethodProtectedParamsPublicReturnType(Integer foo) {
        return self._selfInvokePublicMethodProtectedParamsPublicReturnType(new ProtectedType(foo));
    }

    @Override
    public Integer selfInvokePublicMethodPublicParamsProtectedReturnType(Integer foo) {
        return self._selfInvokePublicMethodPublicParamsProtectedReturnType(foo).getVal();
    }

    @Override
    public Integer selfInvokePublicMethodProtectedParamsProtectedReturnType(Integer foo) {
        return self._selfInvokePublicMethodProtectedParamsProtectedReturnType(new ProtectedType(foo)).getVal();
    }

    @Override
    public Integer selfInvokeProtectedMethodProtectedParamsProtectedReturnType(Integer foo) {
        return self._selfInvokeProtectedMethodProtectedParamsProtectedReturnType(new ProtectedType(foo)).getVal();
    }

    @Override
    public Integer selfInvokeProtectedMethodPublicParamsProtectedReturnType(Integer foo) {
        return self._selfInvokeProtectedMethodPublicParamsProtectedReturnType(foo).getVal();
    }

    @Override
    public Integer selfInvokeProtectedMethodProtectedParamsPublicReturnType(Integer foo) {
        return self._selfInvokeProtectedMethodProtectedParamsPublicReturnType(new ProtectedType(foo));
    }

    @Override
    public Integer selfInvokeProtectedMethodPublicParamsPublicReturnType(Integer foo) {
        return self._selfInvokeProtectedMethodPublicParamsPublicReturnType(foo);
    }

    @SelfIntercept
    public Integer _selfInvokePublicMethodPublicParamsPublicReturnType(Integer foo) {
        return foo;
    }

    @SelfIntercept
    public Integer _selfInvokePublicMethodProtectedParamsPublicReturnType(ProtectedType foo) {
        return foo.getVal();
    }

    @SelfIntercept
    public ProtectedType _selfInvokePublicMethodPublicParamsProtectedReturnType(Integer foo) {
        return new ProtectedType(foo);
    }

    @SelfIntercept
    public ProtectedType _selfInvokePublicMethodProtectedParamsProtectedReturnType(ProtectedType foo) {
        return foo;
    }

    @SelfIntercept
    protected ProtectedType _selfInvokeProtectedMethodProtectedParamsProtectedReturnType(ProtectedType foo) {
        return foo;
    }

    @SelfIntercept
    protected ProtectedType _selfInvokeProtectedMethodPublicParamsProtectedReturnType(Integer foo) {
        return new ProtectedType(foo);
    }

    @SelfIntercept
    protected Integer _selfInvokeProtectedMethodProtectedParamsPublicReturnType(ProtectedType foo) {
        return foo.getVal();
    }

    @SelfIntercept
    protected Integer _selfInvokeProtectedMethodPublicParamsPublicReturnType(Integer foo) {
        return foo;
    }


    /**
     * Used as either a protected return type or a protected param type
     */
    protected class ProtectedType {

        Integer val;

        ProtectedType(Integer value) {
            this.val = value;
        }

        public Integer getVal() {
            return val;
        }
    }

}
