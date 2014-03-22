package org.jboss.weld.environment.se.test.isolation;

public class PinholeCamera implements Camera {

    public static int picturesTaken = 0;

    @Override
    public void capture() {
        picturesTaken++;
    }

}
