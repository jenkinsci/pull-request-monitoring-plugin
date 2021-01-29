package io.jenkins.plugins.monitoring;

import hudson.ExtensionFinder;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Objects;

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

    public List<ExtensionFinder> getExtensions() {
        return Objects.requireNonNull(Jenkins.getInstanceOrNull()).getExtensionList(ExtensionFinder.class);
    }


}
