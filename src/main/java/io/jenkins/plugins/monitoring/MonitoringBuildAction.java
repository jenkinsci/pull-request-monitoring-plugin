package io.jenkins.plugins.monitoring;

import hudson.model.Run;
import hudson.model.User;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private transient Run<?, ?> owner;

    /**
     * Creates a new instance of {@link MonitoringBuildAction}.
     *
     * @param run
     *          the run that owns this action.
     *
     * @param monitor
     *          the {@link Monitor} to be add.
     */
    public MonitoringBuildAction(Run<?, ?> run, Monitor monitor) throws IOException {
        this.owner = run;
        this.monitor = monitor;
    }

    @JavaScriptMethod
    public void updateUserConfiguration(String id, String config) throws IOException {
        User user = User.current();

        if (user == null) {
           return;
        }

        MonitorUserProperty property = user.getProperty(MonitorUserProperty.class);
        MonitorUserProperty.MonitorProperty defaultProp = new MonitorUserProperty.MonitorProperty("default",
                this.monitor.getConfiguration());

        if (property == null) {
            property = new MonitorUserProperty(defaultProp);
            user.addProperty(property);
        }

        if (property.getProperties() == null) {
            List<MonitorUserProperty.MonitorProperty> views = new ArrayList<>();
            views.add(defaultProp);
            property.setProperties(views);
        }

        property.createOrUpdate(id, config);
        user.save();
    }

    @JavaScriptMethod
    public String getConfiguration(String id) throws IOException {
        User user = User.current();

        if (user == null) {
            return this.monitor.getConfiguration();
        }

        MonitorUserProperty property = user.getProperty(MonitorUserProperty.class);

        if (property.getProperties() == null) {
            updateUserConfiguration("default", this.monitor.getConfiguration());
        }

        MonitorUserProperty.MonitorProperty monitorProperty = property.getView(id);

        if (monitorProperty == null) {
            monitorProperty = property.getView("default");
        }

        return monitorProperty.getConfig();
    }

    @Override
    public String getIconFileName() {
        return MonitoringMultibranchProjectAction.getIconSmall();
    }

    @Override
    public String getDisplayName() {
        return String.format("%s '%s'", MonitoringMultibranchProjectAction.getName(), owner.getDisplayName());
    }

    @Override
    public String getUrlName() {
        return MonitoringMultibranchProjectAction.getURI();
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.owner = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.owner = run;
    }

    public Run<?, ?> getRun() {
        return owner;
    }

    public Monitor getMonitor() {
        return monitor;
    }

}
