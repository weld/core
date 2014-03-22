package org.jboss.weld.environment.se.test.isolation;

@Zoom
public class DSLR implements Camera {

    public static int picturesTaken = 0;

    @Override
    public void capture() {
        picturesTaken++;
    }

}
