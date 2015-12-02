package io.cloudsoft.jmeter;

import org.apache.brooklyn.api.effector.Effector;
import org.apache.brooklyn.api.entity.ImplementedBy;
import org.apache.brooklyn.config.ConfigKey;
import org.apache.brooklyn.core.config.ConfigKeys;
import org.apache.brooklyn.core.effector.Effectors;
import org.apache.brooklyn.core.entity.Attributes;
import org.apache.brooklyn.core.sensor.AttributeSensorAndConfigKey;
import org.apache.brooklyn.core.sensor.BasicAttributeSensorAndConfigKey;
import org.apache.brooklyn.entity.software.base.SoftwareProcess;

@ImplementedBy(JMeterNodeImpl.class)
public interface JMeterNode extends SoftwareProcess{

    ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION,
            "2.13");

    AttributeSensorAndConfigKey<String, String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey.StringAttributeSensorAndConfigKey(
            Attributes.DOWNLOAD_URL,
            "http://mirrors.muzzy.org.uk/apache//jmeter/binaries/apache-jmeter-${version}.tgz");

    BasicAttributeSensorAndConfigKey<String> TEST_PLAN_URL = new BasicAttributeSensorAndConfigKey.StringAttributeSensorAndConfigKey(
            "jmeter.testPlan", "A URL of a jmx file to run",
            "classpath://io/cloudsoft/jmeter/load.jmx");

    Effector<Void> RUN_TEST_PLAN = Effectors.effector(Void.class, "runTestPlan")
            .description("Runs the JMeter test plan specified in jmeter.testPlan")
            .buildAbstract();
}
