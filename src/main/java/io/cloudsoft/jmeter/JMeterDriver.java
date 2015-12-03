package io.cloudsoft.jmeter;

import org.apache.brooklyn.entity.java.JavaSoftwareProcessDriver;
import org.apache.brooklyn.util.core.config.ConfigBag;

public interface JMeterDriver extends JavaSoftwareProcessDriver {

    void runTestPlan();

    void reconfigure();

}
