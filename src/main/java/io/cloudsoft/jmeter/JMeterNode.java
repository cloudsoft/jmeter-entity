package io.cloudsoft.jmeter;

import javax.annotation.Nullable;

import org.apache.brooklyn.api.catalog.Catalog;
import org.apache.brooklyn.api.entity.ImplementedBy;
import org.apache.brooklyn.config.ConfigKey;
import org.apache.brooklyn.core.annotation.Effector;
import org.apache.brooklyn.core.annotation.EffectorParam;
import org.apache.brooklyn.core.config.ConfigKeys;
import org.apache.brooklyn.core.effector.MethodEffector;
import org.apache.brooklyn.core.entity.Attributes;
import org.apache.brooklyn.core.sensor.AttributeSensorAndConfigKey;
import org.apache.brooklyn.entity.software.base.SoftwareProcess;
import org.apache.brooklyn.util.core.flags.SetFromFlag;

@Catalog(name = "Apache JMeter",
        description = "Uses Apache JMeter to generate load against a configurable URL",
        iconUrl = "classpath://io/cloudsoft/jmeter/logo.jpg")
@ImplementedBy(JMeterNodeImpl.class)
public interface JMeterNode extends SoftwareProcess {

    ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION,
            "3.1");

    AttributeSensorAndConfigKey<String, String> DOWNLOAD_URL = ConfigKeys.newSensorAndConfigKeyWithDefault(
            Attributes.DOWNLOAD_URL,
            "http://download.nextag.com/apache/jmeter/binaries/apache-jmeter-${version}.tgz");

    @SetFromFlag("plan")
    AttributeSensorAndConfigKey<String, String> TEST_PLAN_URL = ConfigKeys.newStringSensorAndConfigKey(
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
    ConfigKey<Integer> REQUEST_DELAY = ConfigKeys.newIntegerConfigKey(
            "jmeter.requestDelay", "The period each thread should wait between requests in milliseconds",
            100);

    MethodEffector<Void> PAUSE = new MethodEffector<>(JMeterNode.class, "pause");
    @Effector(description = "Stop the JMeter test plan's execution")
    void pause();

    MethodEffector<Void> CHANGE_LOAD = new MethodEffector<>(JMeterNode.class, "changeLoad");
    @Effector(description = "Adjust the generated load. Generated requests/second will be " +
            "roughly newThreadCount * (1000 / newDelayCount)")
    void changeLoad(
            @Nullable
            @EffectorParam(name = "newThreadCount",
                    description = "The number of threads the plan should use",
                    nullable = true)
            Integer newThreadCount,
            @Nullable
            @EffectorParam(name="newDelayCount",
                    description="The period in milliseconds each thread should wait between requests",
                    nullable=true)
            Integer newDelayCount);

}
