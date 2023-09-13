package org.jboss.weld.metadata;

import java.net.URL;

import org.jboss.weld.bootstrap.spi.Metadata;

public class FileMetadata<T> implements Metadata<T> {

    private final T value;
    private final URL file;
    private final int lineNumber;

    public FileMetadata(T value, URL file, int lineNumber) {
        this.value = value;
        this.file = file;
        this.lineNumber = lineNumber;
    }

    public String getLocation() {
        if (value != null) {
            return file.toString() + "@" + lineNumber + "[" + value + "]";
        } else {
            return file.toString() + "@" + lineNumber;
        }
    }

    public T getValue() {
        return value;
    }

    public URL getFile() {
        return file;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return getLocation();
    }

}
