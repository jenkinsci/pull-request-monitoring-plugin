package io.jenkins.plugins.monitoring;

import hudson.model.Run;
import hudson.model.User;
import jenkins.model.RunAction2;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public MonitoringBuildAction(Run<?, ?> run, Monitor monitor) {
        this.owner = run;
        this.monitor = monitor;
    }

    /**
     * Sets the default for {@link MonitorUserProperty}.
     */
    private void setDefaultUserProperty() throws IOException {
        User user = User.current();

        if (user == null) {
            return;
        }

        MonitorUserProperty property = user.getProperty(MonitorUserProperty.class);
        property.update("default", this.monitor.getPortlets());

        user.save();
    }

    /**
     * Saves the actual dashboard configuration to {@link MonitorUserProperty}.
     *
     * @param config
     *              the config string to update.
     *
     */
    @JavaScriptMethod
    public void updateUserConfiguration(String config) throws IOException {
        User user = User.current();

        if (user == null) {
            return;
        }

        MonitorUserProperty property = user.getProperty(MonitorUserProperty.class);
        property.update(getProjectId(), config);
        user.save();
    }

    /**
     * Get the current dashboard configuration of user.
     *
     * @return
     *          the config of the corresponding {@link MonitorUserProperty} of the actual project
     *          or the default configuration of Jenkinsfile if no config exists in {@link MonitorUserProperty}
     *          for actual project.
     */
    @JavaScriptMethod
    public String getConfiguration() throws IOException {
        //todo: Work around, because no user is available when the action is added, so can not do this in ctor.
        setDefaultUserProperty();

        User user = User.current();

        if (user == null) {
            return this.monitor.getPortlets();
        }

        MonitorUserProperty property = user.getProperty(MonitorUserProperty.class);
        MonitorUserProperty.MonitorProperty monitorProperty = property.getProperty(getProjectId());

        return (monitorProperty != null) ? monitorProperty.getConfig() : property.getProperty("default").getConfig();
    }

    /**
     * Get the project it based on the current {@link Run}.
     *
     * @return
     *          the display name of the current project as id.
     */
    public String getProjectId() {
        String id = this.owner.getParent().getParent().getDisplayName();
        return id.toLowerCase().replaceAll(" ", "-");
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

    /**
     * Get all portlets, which are not available anymore.
     *
     * @return
     *          a list of all unavailable portlet ids.
     *
     * @throws IOException
     *          {@link MonitoringBuildAction#getConfiguration()} throws an error.
     */
    public List<String> getUnavailablePortlets() throws IOException {
        JSONArray portlets = new JSONArray(this.getConfiguration());

        List<String> usedPlugins = new ArrayList<>();

        for (Object o : portlets) {
            JSONObject portlet = (JSONObject) o;
            usedPlugins.add(portlet.getString("id"));
        }

        List<String> availablePlugins = this.monitor.getAvailablePortlets(this.owner)
                .stream().map(MonitorPortlet::getId).collect(Collectors.toList());
        return new ArrayList<String>(CollectionUtils.removeAll(usedPlugins, availablePlugins));
    }
}
