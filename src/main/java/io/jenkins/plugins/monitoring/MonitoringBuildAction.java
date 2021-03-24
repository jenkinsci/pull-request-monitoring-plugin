package io.jenkins.plugins.monitoring;

import hudson.model.Action;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.branch.MultiBranchProject;

import java.util.Optional;

public class MonitoringBuildAction implements Action {
    private final transient Run<?, ?> run;

    public MonitoringBuildAction(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public String getIconFileName() {
        return MonitoringProjectAction.ICON_SMALL;
    }

    @Override
    public String getDisplayName() {
        return MonitoringProjectAction.DISPLAY_NAME;
    }

    @Override
    public String getUrlName() {
        return MonitoringProjectAction.URI;
    }

    public Run<?, ?> getRun() {
        return run;
    }
}
