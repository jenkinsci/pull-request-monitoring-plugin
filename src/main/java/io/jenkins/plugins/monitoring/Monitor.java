package io.jenkins.plugins.monitoring;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.monitoring.util.PortletUtils;
import io.jenkins.plugins.monitoring.util.PullRequestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This {@link Step} is responsible for the configuration of the monitoring dashboard
 * via the corresponding Jenkinsfile.
 *
 * @author Simon Symhoven
 */
public final class Monitor extends Step implements Serializable {
    private static final long serialVersionUID = -1329798203887148860L;
    private String portlets;

    /**
     * Creates a new instance of {@link Monitor}.
     *
     * @param portlets
     *              the monitor configuration as json array string.
     */
    @DataBoundConstructor
    public Monitor(final String portlets) {
        super();
        this.portlets = portlets;
    }

    private void setPortlets(String portlets) {
        this.portlets = portlets;
    }

    public String getPortlets() {
        return portlets;
    }

    @Override
    public StepExecution start(final StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    /**
     *  The {@link Execution} routine for the monitoring step.
     */
    static class Execution extends SynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1300005476208035751L;
        private final Monitor monitor;

        Execution(final StepContext stepContext, final Monitor monitor) {
            super(stepContext);
            this.monitor = monitor;
        }

        @Override
        public Void run() throws Exception {
            final Run<?, ?> run = getContext().get(Run.class);

            if (run == null) {
                log("[Monitor] Run not found!");
                return null;
            }

            if (!PortletUtils.isValidConfiguration(monitor.getPortlets())) {
                log("[Monitor] Portlet Configuration is invalid!");
                return null;
            }

            JSONArray portlets = new JSONArray(monitor.getPortlets());
            log("[Monitor] Portlet Configuration: " + portlets.toString(3));

            List<String> classes = PortletUtils.getAvailablePortlets(run)
                    .stream()
                    .map(MonitorPortlet::getId)
                    .collect(Collectors.toList());

            log("[Monitor] Available portlets: ["
                            + StringUtils.join(classes, ", ") + "]");

            List<String> usedPortlets = new ArrayList<>();

            for (Object o : portlets) {
                if (o instanceof JSONObject) {
                    JSONObject portlet = (JSONObject) o;
                    String id = portlet.getString("id");

                    if (usedPortlets.contains(id)) {
                        log("[Monitor] Portlet with ID '" + id
                                + "' already defined in list of portlets. Skip adding this portlet!");
                    }
                    else {
                        usedPortlets.add(id);
                    }
                }
            }

            List<String> missedPortletIds = new ArrayList<String>(CollectionUtils.removeAll(usedPortlets, classes));

            if (!missedPortletIds.isEmpty()) {
                log("[Monitor] Can't find the following portlets "
                                + missedPortletIds + " in list of available portlets! Will remove from current configuration.");

                JSONArray cleanedPortlets = new JSONArray();
                for (Object o : portlets) {
                    JSONObject portlet = (JSONObject) o;
                    if (!missedPortletIds.contains(portlet.getString("id"))) {
                        cleanedPortlets.put(portlet);
                    }
                }

                monitor.setPortlets(cleanedPortlets.toString(3));

                log("[Monitor] Cleaned Portlets: " + cleanedPortlets.toString(3));
            }

            if (PullRequestUtils.isPullRequest(run.getParent())) {
                log("[Monitor] Build is part of a pull request. Add 'MonitoringCustomAction' now.");

                run.addAction(new MonitoringCustomAction(monitor.getPortlets()));
            }
            else {
                log("[Monitor] Build is not part of a pull request. Skip adding 'MonitoringCustomAction'.");
            }

            return null;
        }

        private void log(String message) throws IOException, InterruptedException {
            Objects.requireNonNull(getContext().get(TaskListener.class)).getLogger()
                    .println(message);
        }

    }

    /**
     * A {@link Descriptor} for the monitoring step.
     */
    @Extension
    public static class Descriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class, Run.class);
        }

        @Override
        public String getFunctionName() {
            return "monitoring";
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return Messages.Step_DisplayName();
        }
    }

}
