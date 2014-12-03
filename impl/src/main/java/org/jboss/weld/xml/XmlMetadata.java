/**
 *
 */
package org.jboss.weld.xml;

import org.jboss.weld.metadata.FileMetadata;

import java.net.URL;

public class XmlMetadata<T> extends FileMetadata<T> {

    private final String qName;

    public XmlMetadata(String qName, T value, URL file, int lineNumber) {
        super(value, file, lineNumber);
        this.qName = qName;
    }

    @Override
    public String getLocation() {
        if (getValue() != null) {
            return '<' + qName + '>' + getValue() + "</" + qName + "> in " + getFile().toString() + '@' + getLineNumber();
        } else {
            return '<' + qName + " /> in " + getFile().toString() + '@' + getLineNumber();
        }
    }

    public String getQName() {
        return qName;
    }

}
