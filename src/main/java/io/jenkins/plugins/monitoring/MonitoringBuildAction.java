package io.jenkins.plugins.monitoring;

import hudson.model.Action;
import hudson.model.Run;


/**
 * This action displays a link on the side panel of a {@link Run}. The action is only displayed if the parent job
 * is a pull request, which is described in the associated {@link MonitoringBuildActionFactory}.
 * The action is responsible to render the summary via its associated 'summary.jelly' view and render the
 * main plugin page, where the user can configure the dashboard with all supported plugins via its associated
 * 'index.jelly view.
 *
 * @author Simon Symhoven
 */
public class MonitoringBuildAction implements Action {
    private String config;
    private final transient Run<?, ?> run;

    public MonitoringBuildAction(Run<?, ?> run) {
        this.run = run;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getConfig() {
        return config;
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

}
