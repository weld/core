package org.jboss.weld.util.bytecode;

import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Deferred bytecode that can be applied to a BlockCreator.
 * This is used to generate bytecode that needs to be inserted at specific points,
 * such as field initialization in constructors.
 *
 * @author Stuart Douglas
 */
@FunctionalInterface
public interface DeferredBytecode {

    /**
     * Applies this deferred bytecode to the given block creator.
     *
     * @param blockCreator the block creator to apply bytecode to
     */
    void apply(BlockCreator blockCreator);

}
