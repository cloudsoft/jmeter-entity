package io.cloudsoft.jmeter;

import org.apache.brooklyn.api.entity.Entity;
import org.apache.brooklyn.core.effector.EffectorBody;
import org.apache.brooklyn.entity.software.base.SoftwareProcessDriver;
import org.apache.brooklyn.entity.software.base.SoftwareProcessImpl;
import org.apache.brooklyn.util.core.config.ConfigBag;

public class JMeterNodeImpl extends SoftwareProcessImpl implements JMeterNode {
    @Override
    public Class getDriverInterface() {
        return JMeterDriver.class;
    }

    @Override
    public void init() {
        super.init();
        getMutableEntityType().addEffector(RUN_TEST_PLAN, new EffectorBody<Void>() {
            @Override
            public Void call(ConfigBag configBag) {
                runTestPlan(configBag);
                return null;
            }
        });
    }

    @Override
    protected void connectSensors() {
        super.connectSensors();
        super.connectServiceUpIsRunning();
    }

    @Override
    public JMeterDriver getDriver() {
        return (JMeterDriver)super.getDriver();
    }

    private void runTestPlan(ConfigBag configBag) {
        getDriver().runTestPlan(configBag);
    }
}
