package io.jenkins.plugins.monitoring;

import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.model.ProminentProjectAction;
import hudson.util.RunList;

public class MonitoringProjectAction implements ProminentProjectAction {
    static final String URI = "pull-request-monitoring";
    static final String DISPLAY_NAME = "Pull Request Monitoring";
    static final String ICONS_PREFIX = "/plugin/pull-request-monitoring/icons/";
    static final String ICON_SMALL = ICONS_PREFIX + "pull-request-monitoring-24x24.png";
    static final String ICON_BIG = ICONS_PREFIX + "pull-request-monitoring-48x48.png";

    private transient final Project<?, ?> project;

    public MonitoringProjectAction(Project<?, ?> project) {
        this.project = project;
    }

    @Override
    public String getIconFileName() {
        return ICON_BIG;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getUrlName() {
        return URI;
    }

    public Project<?, ?> getProject() {
        return project;
    }

    public RunList<?> getBuilds() {
        return project.getBuilds();
    }


}
