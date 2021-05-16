package io.jenkins.plugins.monitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Run;
import jenkins.model.RunAction2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
public class MonitoringDefaultAction implements RunAction2 {
    private final Monitor monitor;
    private transient Run<?, ?> run;

    /**
     * Creates a new instance of {@link MonitoringDefaultAction}.
     *
     * @param run
     *          the run that owns this action.
     *
     * @param monitor
     *          the {@link Monitor} to be add.
     */
    public MonitoringDefaultAction(Run<?, ?> run, Monitor monitor) {
        this.run = run;
        this.monitor = monitor;
    }

    @Override
    public String getIconFileName() {
        return MonitoringMultibranchProjectAction.getIconSmall();
    }

    @Override
    public String getDisplayName() {
        return String.format("%s '%s'", MonitoringMultibranchProjectAction.getName(), getRun().getDisplayName());
    }

    @Override
    public String getUrlName() {
        return MonitoringMultibranchProjectAction.getURI();
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    /**
     * Get the project it based on the current {@link Run}.
     *
     * @return
     *          the display name of the current project as id.
     */
    public String getConfigurationId() {
        String id = getRun().getParent().getParent().getDisplayName();
        return id.toLowerCase().replaceAll(" ", "-");
    }

    /**
     * Sets the default for {@link MonitorConfigurationProperty}.
     */
    private void setDefaultMonitorConfiguration() {
        MonitorConfigurationProperty
                .forCurrentUser()
                .ifPresent(monitorConfigurationProperty -> monitorConfigurationProperty
                        .createOrUpdateConfiguration(MonitorConfigurationProperty.DEFAULT_ID, resolvePortlets()));
    }

    /**
     * Get all portlets, which are not available anymore.
     *
     * @return
     *          a list of all unavailable portlet ids.
     *
     * @throws IOException
     *          {@link MonitoringDefaultAction#getConfiguration()} throws an error.
     */
    public List<String> getUnavailablePortlets() throws IOException {
        JSONArray portlets = new JSONArray(getConfiguration());

        List<String> usedPlugins = new ArrayList<>();

        for (Object o : portlets) {
            JSONObject portlet = (JSONObject) o;
            usedPlugins.add(portlet.getString("id"));
        }

        List<String> availablePlugins = getMonitor().getAvailablePortlets(getRun())
                .stream().map(MonitorPortlet::getId).collect(Collectors.toList());
        return new ArrayList<String>(CollectionUtils.removeAll(usedPlugins, availablePlugins));
    }

    /**
     * Checks if there are changes in the configuration since the last build.
     *
     * @return
     *          true if both configurations are equal, else false.
     */
    public boolean hasChanges() {
        MonitoringCustomAction action = getRun().getAction(MonitoringCustomAction.class);

        if (action == null) {
            return false;
        }

        Run<?, ?> previous = getRun().getPreviousBuild();

        if (previous == null) {
            return false;
        }

        MonitoringCustomAction prevAction = previous.getAction(MonitoringCustomAction.class);

        if (prevAction == null) {
            return false;
        }

        return !areJsonNodesEquals(action.getMonitor().getPortlets(), prevAction.getMonitor().getPortlets());
    }

    /**
     * Compares to json nodes, if they are equals.
     *
     * @param s1
     *          the first json node as string.
     *
     * @param s2
     *          the second json node as string.
     *
     * @return
     *          true, if both are equals, else false.
     */
    public boolean areJsonNodesEquals(String s1, String s2) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node1 = mapper.readTree(s1);
            JsonNode node2 = mapper.readTree(s2);
            return node1.equals(node2);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
        JavaScriptMethod block. All methods are called from a jelly file.
     */

    /**
     * Saves the actual dashboard configuration to {@link MonitorConfigurationProperty}.
     *
     * @param config
     *              the config string to update.
     *
     */
    @JavaScriptMethod
    public void updateMonitorConfiguration(String config) {
        MonitorConfigurationProperty
                .forCurrentUser()
                .ifPresent(monitorConfigurationProperty ->
                        monitorConfigurationProperty.createOrUpdateConfiguration(getConfigurationId(), config));
    }

    /**
     * Get the current dashboard configuration of user.
     *
     * @return
     *          the config of the corresponding {@link MonitorConfigurationProperty} of the actual project
     *          or the default configuration of Jenkinsfile if no config exists in {@link MonitorConfigurationProperty}
     *          for actual project.
     */
    @JavaScriptMethod
    public String getConfiguration() throws IOException {
        //todo: Work around, because no user is available when the action is added, so can not do this in ctor.
        setDefaultMonitorConfiguration();

        MonitorConfigurationProperty monitorConfigurationProperty = MonitorConfigurationProperty
                .forCurrentUser().orElse(null);

        return monitorConfigurationProperty == null
                ? StringUtils.EMPTY : monitorConfigurationProperty.getConfiguration(getConfigurationId()).getConfig();
    }

    /**
     * Checks if the actual configuration is synced with the default one (Jenkinsfile or empty one).
     *
     * @return
     *              true, if synced, else false.
     */
    @JavaScriptMethod
    public boolean isMonitorConfigurationSynced() {

        MonitorConfigurationProperty monitorConfigurationProperty = MonitorConfigurationProperty
                .forCurrentUser().orElse(null);

        if (monitorConfigurationProperty == null) {
            return true;
        }

        MonitorConfigurationProperty.MonitorConfiguration projectConfiguration =
                monitorConfigurationProperty.getConfiguration(getConfigurationId());

        MonitorConfigurationProperty.MonitorConfiguration defaultConfiguration =
                monitorConfigurationProperty.getConfiguration(MonitorConfigurationProperty.DEFAULT_ID);

        if (projectConfiguration == null) {
            return true;
        }

        return areJsonNodesEquals(projectConfiguration.getConfig(), defaultConfiguration.getConfig());
    }

    /**
     * Resolves the portlet string of the {@link Monitor}.
     *
     * @return
     *          the portlet string of {@link MonitoringCustomAction} if exists,
     *          else the one of {@link MonitoringDefaultAction}.
     */
    @JavaScriptMethod
    public String resolvePortlets() {
        MonitoringCustomAction action = getRun().getAction(MonitoringCustomAction.class);
        return action != null ? action.getMonitor().getPortlets() : getMonitor().getPortlets();
    }

    /**
     * Reset the current project configuration to default.
     */
    @JavaScriptMethod
    public void resetMonitorConfiguration() {
        MonitorConfigurationProperty
                .forCurrentUser()
                .ifPresent(monitorConfigurationProperty -> monitorConfigurationProperty.removeConfiguration(getConfigurationId()));
    }

}
