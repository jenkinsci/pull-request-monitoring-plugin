package io.jenkins.plugins.monitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.hm.hafner.util.FilteredLog;
import hudson.model.Run;
import io.jenkins.plugins.forensics.reference.ReferenceFinder;
import io.jenkins.plugins.monitoring.util.PortletUtils;
import j2html.tags.DomContent;
import jenkins.model.Jenkins;
import jenkins.model.RunAction2;
import jenkins.scm.api.metadata.ContributorMetadataAction;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

/**
 * This action displays a link on the side panel of a {@link Run}. The action is only displayed if the parent job
 * is a pull request.
 * The action is responsible to render the summary via its associated 'summary.jelly' view and render the
 * main plugin page, where the user can configure the dashboard with all supported plugins via its associated
 * 'index.jelly view.
 *
 * @author Simon Symhoven
 */
public class MonitoringDefaultAction implements RunAction2, StaplerProxy {
    private static final Logger LOGGER = Logger.getLogger(MonitoringDefaultAction.class.getName());
    private transient Run<?, ?> run;

    /**
     * Creates a new instance of {@link MonitoringDefaultAction}.
     *
     * @param run
     *          the run that owns this action.
     */
    public MonitoringDefaultAction(final Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public String getIconFileName() {
        return MonitoringMultibranchProjectAction.getIconSmall();
    }

    @Override
    public String getDisplayName() {
        return String.format("%s '%s'", Messages.BuildAction_Name(), getRun().getDisplayName());
    }

    @Override
    public String getUrlName() {
        return MonitoringMultibranchProjectAction.getURI();
    }

    @Override
    public void onAttached(final Run<?, ?> build) {
        this.run = build;
    }

    @Override
    public void onLoad(final Run<?, ?>  build) {
        this.run = build;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public String getPortlets() {
        return PortletUtils.getDefaultPortletsAsConfiguration(getRun());
    }

    /**
     * Get the {@link ObjectMetadataAction} for the current build.
     *
     * @return
     *          the {@link ObjectMetadataAction}.
     */
    public Optional<ObjectMetadataAction> getObjectMetadataAction() {
        return Optional.ofNullable(getRun().getParent().getProperty(BranchJobProperty.class)
                .getBranch().getAction(ObjectMetadataAction.class));
    }

    /**
     * Get the {@link ContributorMetadataAction} for the current build.
     *
     * @return
     *          the {@link ContributorMetadataAction}.
     */
    public Optional<ContributorMetadataAction> getContributorMetadataAction() {
        return Optional.ofNullable(getRun().getParent().getProperty(BranchJobProperty.class)
                .getBranch().getAction(ContributorMetadataAction.class));
    }

    /**
     * Get the {@link ChangeRequestSCMHead2} for a specific {@link Run}.
     *
     * @return
     *          the {@link ChangeRequestSCMHead2} of run.
     */
    public ChangeRequestSCMHead2 getScmHead() {
        return (ChangeRequestSCMHead2) getRun().getParent().getProperty(BranchJobProperty.class).getBranch().getHead();
    }

    /**
     * Creates the heading for the pull request metadata accordion.
     *
     * @return
     *          the title as html element.
     */
    public String getPullRequestMetadataTitle() {

        Optional<ContributorMetadataAction> contributorMetadataAction = getContributorMetadataAction();

        return span(join(
                strong(iffElse(contributorMetadataAction.isPresent(),
                        getContributorMetadataAction().get().getContributor(),
                        "unknown")), "wants to merge",
                strong(getScmHead().getOriginName()), "into",
               strong(getScmHead().getTarget().getName()))).render();
    }

    /**
     * Creates the reference build link for the pull request metadata title.
     *
     * @return
     *          the reference build as html element.
     */
    public String getPullRequestReferenceBuildDescription() {
        Optional<Run<?, ?>> referenceBuild = getReferenceBuild();

        String url = referenceBuild.map(value -> Jenkins.get().getRootUrl() + value.getUrl()).orElse("#");
        String name = referenceBuild.map(Run::getDisplayName).orElse("#?");

        String target = getScmHead().getTarget().getName();

        DomContent reference = join("Reference Build:", a().withId("reference-build-link")
                    .withHref(url)
                    .withText(target + " " + name));

        return span(reference).render();
    }

    /**
     * Get the reference build to current build.
     *
     * @return
     *          the reference build as {@link Optional}.
     */
    private Optional<Run<?, ?>> getReferenceBuild() {
        FilteredLog log = new FilteredLog("");
        ReferenceFinder referenceFinder = new ReferenceFinder();
        return referenceFinder.findReference(getRun(), log);
    }

    /**
     * Creates the body for the pull request metadata accordion.
     *
     * @return
     *          the body as html element.
     */
    public String getPullRequestMetadataBody() {
        if (!getObjectMetadataAction().isPresent()) {
            return span(i("No metadata found.")).render();
        }

        String description = getObjectMetadataAction().get().getObjectDescription();

        if (StringUtils.isEmpty(description)) {
            return span(i("No description provided.")).render();
        }

        Parser parser = Parser.builder().build();
        Node document = parser.parse(description);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
    }

    /**
     * Get the project it based on the current {@link Run}.
     *
     * @return
     *          the display name of the current project as id.
     */
    public String getConfigurationId() {
        String id = getRun().getParent().getParent().getDisplayName();
        return StringUtils.toRootLowerCase(id).replaceAll(" ", "-");
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
     */
    @SuppressWarnings("unchecked")
    public List<String> getUnavailablePortlets() {
        JSONArray portlets = new JSONArray(getConfiguration());

        List<String> usedPlugins = new ArrayList<>();

        for (Object o : portlets) {
            JSONObject portlet = (JSONObject) o;
            usedPlugins.add(portlet.getString("id"));
        }

        List<String> availablePlugins = PortletUtils.getAvailablePortlets(getRun())
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

        return !areJsonNodesEquals(action.getPortlets(), prevAction.getPortlets());
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
    public boolean areJsonNodesEquals(final String s1, final String s2) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node1 = mapper.readTree(s1);
            JsonNode node2 = mapper.readTree(s2);
            return node1.equals(node2);
        }
        catch (JsonProcessingException exception) {
            LOGGER.log(Level.SEVERE, "Json could not be parsed: ", exception);
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
    public void updateMonitorConfiguration(final String config) {
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
    public String getConfiguration() {

        MonitorConfigurationProperty monitorConfigurationProperty = MonitorConfigurationProperty
                .forCurrentUser().orElse(null);

        return monitorConfigurationProperty == null
                ? resolvePortlets() : monitorConfigurationProperty.getConfiguration(getConfigurationId()).getConfig();
    }

    /**
     * Checks if the actual configuration is synced with the default one (Jenkinsfile or empty one).
     *
     * @return
     *              true, if synced, else false.
     */
    @JavaScriptMethod
    public boolean isMonitorConfigurationSynced() {

        MonitorConfigurationProperty monitorConfigurationProperty = MonitorConfigurationProperty.forCurrentUser()
                .orElse(null);

        if (monitorConfigurationProperty == null) {
            return true;
        }

        MonitorConfigurationProperty.MonitorConfiguration projectConfiguration =
                monitorConfigurationProperty.getConfiguration(getConfigurationId());

        if (projectConfiguration == null) {
            return true;
        }

        MonitorConfigurationProperty.MonitorConfiguration defaultConfiguration =
                monitorConfigurationProperty.getConfiguration(MonitorConfigurationProperty.DEFAULT_ID);

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
        return action == null ? getPortlets() : action.getPortlets();
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

    @Override
    public Object getTarget() {
        setDefaultMonitorConfiguration();
        return this;
    }

    /**
     * Gets all {@link MonitorPortlet} for corresponding {@link MonitorPortletFactory}.
     *
     * @param build
     *          the reference build.
     *
     * @return
     *          all available {@link MonitorPortlet}.
     */
    public static List<? extends MonitorPortlet> getAvailablePortlets(final Run<?, ?> build) {
        return PortletUtils.getAvailablePortlets(build);
    }

    /**
     * Get all portlet factories, type of {@link MonitorPortletFactory}.
     *
     * @return
     *          all factories as list.
     */
    public static List<? extends MonitorPortletFactory> getFactories() {
        return PortletUtils.getFactories();
    }

    /**
     * Gets all {@link MonitorPortlet} for one {@link MonitorPortletFactory}.
     *
     * @param build
     *         the build to get the portlets for.
     *
     * @param factory
     *         the factory to get the portlets for.
     *
     * @return
     *         the filtered portlets.
     */
    public static List<? extends MonitorPortlet> getAvailablePortletsForFactory(
            final Run<?, ?> build, final MonitorPortletFactory factory) {
        return PortletUtils.getAvailablePortletsForFactory(build, factory);
    }
}
