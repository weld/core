package org.jboss.weld.tests.invokable.async.broken;

import java.util.concurrent.CompletionStage;

public class IndirectReturnTypeHandler<T> extends IndirectReturnTypeBase<CompletionStage<T>> {
    @Override
    public CompletionStage<T> transform(CompletionStage<T> original, Runnable completion) {
        return original.whenComplete((v, e) -> completion.run());
    }
}
