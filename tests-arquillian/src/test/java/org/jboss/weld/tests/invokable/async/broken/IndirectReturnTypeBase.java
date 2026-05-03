package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.invoke.AsyncHandler;

abstract class IndirectReturnTypeBase<T> implements AsyncHandler.ReturnType<T> {
}
