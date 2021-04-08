package io.jenkins.plugins.monitoring;

import hudson.model.Run;
import jenkins.model.RunAction2;
import org.json.JSONObject;
import org.kohsuke.stapler.bind.JavaScriptMethod;


/**
 * This action displays a link on the side panel of a {@link Run}. The action is only displayed if the parent job
 * is a pull request.
 * The action is responsible to render the summary via its associated 'summary.jelly' view and render the
 * main plugin page, where the user can configure the dashboard with all supported plugins via its associated
 * 'index.jelly view.
 *
 * @author Simon Symhoven
 */
public class MonitoringBuildAction implements RunAction2 {
    private final Monitor monitor;
    private transient Run<?, ?> run;

    public MonitoringBuildAction(Run<?, ?> run, Monitor monitor) {
        this.run = run;
        this.monitor = monitor;
    }

    @JavaScriptMethod
    public String getConfiguration() {
        return monitor.getConfiguration();
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

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }


}
