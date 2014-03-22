package org.jboss.weld.environment.se.test.isolation;

import javax.enterprise.inject.Alternative;

@Alternative
public class RangefinderCamera implements Camera {

    public static int picturesTaken = 0;

    @Override
    public void capture() {
        picturesTaken++;
    }

}
