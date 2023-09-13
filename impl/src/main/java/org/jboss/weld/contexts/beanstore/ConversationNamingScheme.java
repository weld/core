package org.jboss.weld.contexts.beanstore;

import org.jboss.weld.serialization.BeanIdentifierIndex;

/**
 * <p>
 * A naming scheme which can have it's cid swapped out.
 * </p>
 * <p/>
 * <p>
 * This naming scheme is not thread safe
 * </p>
 *
 * @author pmuir
 */
public class ConversationNamingScheme extends BeanIdentifierIndexNamingScheme {

    public static final String PARAMETER_NAME = ConversationNamingScheme.class.getName();

    private String cid;
    private final String prefixBase;

    public ConversationNamingScheme(String prefixBase, String cid, BeanIdentifierIndex index) {
        super("#", index);
        this.cid = cid;
        this.prefixBase = prefixBase;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    @Override
    protected String getPrefix() {
        return prefixBase + "." + cid;
    }

}
