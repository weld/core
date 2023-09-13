package org.jboss.weld.environment.servlet.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import jakarta.servlet.ServletContext;

public class Servlets {

    private Servlets() {
    }

    public static File getRealFile(ServletContext servletContext, String path) throws MalformedURLException {
        String realPath = servletContext.getRealPath(path);
        if (realPath == null) {//WebLogic!
            URL resourcePath = servletContext.getResource(path);
            if ((resourcePath != null) && (resourcePath.getProtocol().equals("file"))) {
                realPath = resourcePath.getPath();
            }
        }

        if (realPath != null) {
            File file = new File(realPath);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

}
