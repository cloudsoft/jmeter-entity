package io.cloudsoft.jmeter;

import static java.lang.String.format;

import java.util.List;

import org.apache.brooklyn.api.entity.EntityLocal;
import org.apache.brooklyn.core.entity.Entities;
import org.apache.brooklyn.entity.java.JavaSoftwareProcessSshDriver;
import org.apache.brooklyn.location.ssh.SshMachineLocation;
import org.apache.brooklyn.util.net.Urls;
import org.apache.brooklyn.util.os.Os;
import org.apache.brooklyn.util.ssh.BashCommands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class JMeterSshDriver extends JavaSoftwareProcessSshDriver implements JMeterDriver {

    public JMeterSshDriver(EntityLocal entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void preInstall() {
        resolver = Entities.newDownloader(this);
        setExpandedInstallDir(Os.mergePaths(getInstallDir(),
                resolver.getUnpackedDirectoryName(format("apache-jmeter-%s", getVersion()))));
    }

    @Override
    public void install() {
        List<String> urls = resolver.getTargets();
        String saveAs = resolver.getFilename();

        List<String> commands = ImmutableList.<String>builder()
                .addAll(BashCommands.commandsToDownloadUrlsAs(urls, saveAs))
                .add("tar xvfz " + saveAs)
                .build();

        newScript(INSTALLING)
                .failOnNonZeroResultCode()
                .body.append(commands).execute();
    }

    @Override
    public void customize() {
        String template = entity.getConfig(JMeterNode.TEST_PLAN_URL);
        copyTemplate(template, getTestPlanLocation(), true, ImmutableMap.<String,String>of());
    }

    @Override
    public void launch() {
        newScript("run-test-plan").failOnNonZeroResultCode().body.append(getLaunchCommand())
                .gatherOutput()
                .failOnNonZeroResultCode()
                .execute();
    }

    @Override
    public boolean isRunning() {
        StringBuilder command = new StringBuilder(getExpandedInstallDir())
                .append("/bin/jmeter -v");
        return newScript(CHECK_RUNNING).failOnNonZeroResultCode().body.append(command)
                .execute() == 0;
    }

    @Override
    public void stop() {
        newScript(STOPPING).body.append(getStopCommand()).execute();
    }

    protected String getTestPlanLocation() {
        return Urls.mergePaths(getRunDir(), "load.jmx");
    }

    @Override
    public String getLogFileLocation() {
        return Urls.mergePaths(getRunDir(), "log.jtl");
    }

    @Override
    public void reconfigure(boolean restartProcess) {
        customize();
        if (restartProcess) {
            newScript("Reloading").failOnNonZeroResultCode()
                    .body.append(getStopCommand(), getLaunchCommand())
                    .execute();
        }
    }

    protected CharSequence getLaunchCommand() {
        return new StringBuilder("nohup ")
                .append(getExpandedInstallDir())
                .append("/bin/jmeter ")
                .append("-n ") // Non-GUI mode
                .append("-t ").append(getTestPlanLocation()).append(" ") // Test plan to run
                .append("-l ").append(getLogFileLocation()).append(" ")  // Log file location
                .append(" >/dev/null 2>/dev/null &");
    }

    protected CharSequence getStopCommand() {
        // Proper way to do it is to use shutdown.sh (graceful) or stoptest.sh (abrupt).
        return "ps aux | grep ApacheJMeter | grep -v grep | awk '{ print $2 }' | xargs kill -15";
    }

}