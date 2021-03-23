package io.jenkins.plugins.monitoring;

import hudson.model.ProminentProjectAction;
import jenkins.branch.MultiBranchProject;

public class MonitoringProjectAction implements ProminentProjectAction {
    static final String URI = "pull-request-monitoring";
    static final String DISPLAY_NAME = "Pull Request Monitoring";
    static final String ICONS_PREFIX = "/plugin/pull-request-monitoring/icons/";
    static final String ICON_SMALL = ICONS_PREFIX + "pull-request-monitoring-24x24.png";
    static final String ICON_BIG = ICONS_PREFIX + "pull-request-monitoring-48x48.png";

    private transient final MultiBranchProject<?, ?> multiBranchProject;

    public MonitoringProjectAction(MultiBranchProject<?, ?> multiBranchProject) {
        this.multiBranchProject = multiBranchProject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconFileName() {
        return ICON_BIG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrlName() {
        return URI;
    }

    public MultiBranchProject<?, ?> getProject() {
        return multiBranchProject;
    }

}
