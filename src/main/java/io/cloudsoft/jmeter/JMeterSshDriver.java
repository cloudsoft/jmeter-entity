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
        newScript(CUSTOMIZING)
                .body.append("mkdir -p " + getRunDir())
                .failOnNonZeroResultCode()
                .execute();
        copyPlan();
    }

    private void copyPlan() {
        copyTemplate(entity.getConfig(JMeterNode.TEST_PLAN_URL), getTestPlanLocation());
    }

    @Override
    public void launch() {
        // NO OP - is run in the effector
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
        // NO OP
    }

    protected String getTestPlanLocation() {
        return Urls.mergePaths(getRunDir(), "load.jmx");
    }

    @Override
    public String getLogFileLocation() {
        return Urls.mergePaths(getRunDir(), "log.jtl");
    }

    @Override
    public void runTestPlan() {
        StringBuilder command = new StringBuilder(getExpandedInstallDir())
                .append("/bin/jmeter ")
                .append("-n ") // Non-GUI mode
                .append("-t ").append(getTestPlanLocation()).append(" ") // Test plan to run
                .append("-l ").append(getLogFileLocation()).append(" "); // Log file location
        newScript("run-test-plan").failOnNonZeroResultCode().body.append(command)
                .gatherOutput()
                .failOnNonZeroResultCode()
                .execute();
    }

    @Override
    public void reconfigure() {
        copyPlan();
    }

}