package io.cloudsoft.jmeter;

import org.apache.brooklyn.api.entity.ImplementedBy;
import org.apache.brooklyn.config.ConfigKey;
import org.apache.brooklyn.core.annotation.Effector;
import org.apache.brooklyn.core.annotation.EffectorParam;
import org.apache.brooklyn.core.config.ConfigKeys;
import org.apache.brooklyn.core.effector.MethodEffector;
import org.apache.brooklyn.core.entity.Attributes;
import org.apache.brooklyn.core.sensor.AttributeSensorAndConfigKey;
import org.apache.brooklyn.core.sensor.BasicAttributeSensorAndConfigKey;
import org.apache.brooklyn.entity.software.base.SoftwareProcess;
import org.apache.brooklyn.util.core.flags.SetFromFlag;

@ImplementedBy(JMeterNodeImpl.class)
public interface JMeterNode extends SoftwareProcess {

    ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION,
            "2.13");

    AttributeSensorAndConfigKey<String, String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey.StringAttributeSensorAndConfigKey(
            Attributes.DOWNLOAD_URL,
            "http://mirrors.muzzy.org.uk/apache//jmeter/binaries/apache-jmeter-${version}.tgz");

    @SetFromFlag("plan")
    BasicAttributeSensorAndConfigKey<String> TEST_PLAN_URL = new BasicAttributeSensorAndConfigKey.StringAttributeSensorAndConfigKey(
            "jmeter.testPlan", "A URL of a jmx file to run",
            "classpath://io/cloudsoft/jmeter/load.jmx");

    @SetFromFlag("runForever")
    ConfigKey<Boolean> RUN_FOREVER = ConfigKeys.newBooleanConfigKey(
            "jmeter.runForever", "Whether JMeter should run until instructed to stop. Overrides jmeter.numLoops.",
            true);

    @SetFromFlag("loops")
    ConfigKey<Integer> NUM_LOOPS = ConfigKeys.newIntegerConfigKey(
            "jmeter.numLoops", "The number of loops each thread should make",
            100);

    @SetFromFlag("numThreads")
    ConfigKey<Integer> NUM_THREADS = ConfigKeys.newIntegerConfigKey(
            "jmeter.numThreads", "The number of threads JMeter should use",
            10);

    @SetFromFlag("requestDelay")
    ConfigKey<Long> REQUEST_DELAY = ConfigKeys.newLongConfigKey(
            "jmeter.requestDelay", "The period each thread should wait between requests in milliseconds",
            100L);

    MethodEffector<Void> PAUSE = new MethodEffector<>(JMeterNode.class, "pause");
    @Effector(description = "Stop the JMeter test plan's execution")
    void pause();

    MethodEffector<Void> INCREASE_LOAD = new MethodEffector<>(JMeterNode.class, "increaseLoad");

    @Effector(description = "Increase generated load")
    void increaseLoad(
            @EffectorParam(name = "threadStep",
                    description = "The number of threads to change by when increasing/decreasing load",
                    defaultValue = "2",
                    nullable = false)
            int threadStep,
            @EffectorParam(name="delayStep",
                    description="The delay to change by when increasing/decreasing load",
                    defaultValue="10",
                    nullable=false)
            int delayStep);

    MethodEffector<Void> DECREASE_LOAD = new MethodEffector<>(JMeterNode.class, "decreaseLoad");
    @Effector(description = "Decrease generated load")
    void decreaseLoad(
            @EffectorParam(name = "threadStep",
                    description = "The number of threads to change by when increasing/decreasing load",
                    defaultValue = "2",
                    nullable = false)
            int threadStep,
            @EffectorParam(name="delayStep",
                    description="The delay to change by when increasing/decreasing load",
                    defaultValue="10",
                    nullable=false)
            int delayStep);

}
