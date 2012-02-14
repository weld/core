package org.jboss.weld.util.bytecode;

import org.jboss.classfilewriter.code.CodeAttribute;

/**
 * @author Stuart Douglas
 */
public interface DeferredBytecode {

    void apply(CodeAttribute codeAttribute);

}
