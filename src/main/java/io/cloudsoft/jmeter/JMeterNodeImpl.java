package io.cloudsoft.jmeter;

import javax.annotation.concurrent.GuardedBy;

import org.apache.brooklyn.entity.software.base.SoftwareProcessImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMeterNodeImpl extends SoftwareProcessImpl implements JMeterNode {

    private static final Logger LOG = LoggerFactory.getLogger(JMeterNodeImpl.class);

    private static final Object reconfigureLock = new Object[0];

    @Override
    protected void connectSensors() {
        super.connectSensors();
        connectServiceUpIsRunning();
    }

    @Override
    protected void disconnectSensors() {
        disconnectServiceUpIsRunning();
        super.disconnectSensors();
    }

    @Override
    public Class getDriverInterface() {
        return JMeterDriver.class;
    }

    @Override
    public JMeterDriver getDriver() {
        return (JMeterDriver) super.getDriver();
    }

    @Override
    public void pause() {
        if (getDriver() != null) {
            getDriver().stop();
        }
    }

    /**
     * Copies new configuration and cycles the JMeter process.
     */
    private void reconfigure() {
        if (getDriver() != null) {
            getDriver().reconfigure(true);
            LOG.debug("Reconfigured and restarted JMeter");
        }
    }

    @Override
    @GuardedBy("reconfigureLock")
    public void changeLoad(Integer newThreadCount, Integer newThreadDelay) {
        synchronized (reconfigureLock) {
            Integer currentThreadCount = getConfig(NUM_THREADS);
            Integer currentThreadDelay = getConfig(REQUEST_DELAY);
            newThreadCount = (newThreadCount != null) ? Math.max(1, newThreadCount) : currentThreadCount;
            newThreadDelay = (newThreadDelay != null) ? Math.max(0, newThreadDelay) : currentThreadDelay;

            // Increase number of threads and decrease the time each one waits
            config().set(NUM_THREADS, newThreadCount);
            config().set(REQUEST_DELAY, newThreadDelay);
            LOG.info("{} changing load generated: numThreads={}, delay={}, approximately {} requests/second",
                    new Object[]{this, newThreadCount, newThreadDelay, getPotentialRequestsPerSecond(newThreadCount, newThreadDelay)});
            reconfigure();
        }
    }

    private long getPotentialRequestsPerSecond(int numThreads, long delay) {
        delay = delay == 0 ? 1 : delay;
        return numThreads * (1000 / delay);
    }

}
