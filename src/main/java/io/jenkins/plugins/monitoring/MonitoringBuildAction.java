package io.jenkins.plugins.monitoring;

import hudson.model.Action;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.Objects;

public class MonitoringBuildAction implements Action {
    private final transient Run<?, ?> run;

    public MonitoringBuildAction(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public String getIconFileName() {
        return MonitoringMultibranchProjectAction.ICON_SMALL;
    }

    @Override
    public String getDisplayName() {
        return String.format("%s '%s'", MonitoringMultibranchProjectAction.DISPLAY_NAME, run.getDisplayName());
    }

    @Override
    public String getUrlName() {
        return MonitoringMultibranchProjectAction.URI;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public boolean isPullRequest() {
        return run.getParent().getPronoun().equals("Pull Request");
    }
}
