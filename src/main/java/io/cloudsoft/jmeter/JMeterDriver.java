package io.cloudsoft.jmeter;

import org.apache.brooklyn.entity.java.JavaSoftwareProcessDriver;

public interface JMeterDriver extends JavaSoftwareProcessDriver {

    void reconfigure();

}
