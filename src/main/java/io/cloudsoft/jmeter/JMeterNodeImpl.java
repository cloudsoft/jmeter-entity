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
    @GuardedBy("reconfigureLock")
    private void reconfigure() {
        if (getDriver() != null) {
            getDriver().reconfigure();
            pause();
            getDriver().start();
            LOG.debug("Reconfigured and restarted JMeter");
        }
    }

    @Override
    public void increaseLoad() {
        synchronized (reconfigureLock) {
            // Increase number of threads and decrease the time each one waits
            int numThreads = getConfig(NUM_THREADS) + getThreadStep();
            setConfig(NUM_THREADS, numThreads);
            Long delay = Math.max(0, getConfig(REQUEST_DELAY) - getDelayStep());
            setConfig(REQUEST_DELAY, delay);
            LOG.info("{} increasing load generated in future runs of plan: numThreads={}, delay={}, potential requests/second={}",
                    new Object[]{this, numThreads, delay, getPotentialRequestsPerSecond(numThreads, delay)});
            reconfigure();
        }
    }

    @Override
    public void decreaseLoad() {
        synchronized (reconfigureLock) {
            // Decrease number of threads and increase the time each one waits
            Integer numThreads = Math.max(1, getConfig(NUM_THREADS) - getThreadStep());
            long delay = getConfig(REQUEST_DELAY) + getDelayStep();
            setConfig(NUM_THREADS, numThreads);
            setConfig(REQUEST_DELAY, delay);
            LOG.info("{} decreasing load generated in future runs of plan: numThreads={}, delay={}, potential requests/second={}",
                    new Object[]{this, numThreads, delay, getPotentialRequestsPerSecond(numThreads, delay)});
            reconfigure();
        }
    }

    private long getPotentialRequestsPerSecond(int numThreads, long delay) {
        return numThreads * (1000 / delay);
    }

    private int getThreadStep() {
        return getConfig(THREAD_STEP);
    }

    private long getDelayStep() {
        return getConfig(DELAY_STEP);
    }

}
