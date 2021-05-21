package io.jenkins.plugins.monitoring;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This {@link Step} is responsible for the configuration of the monitoring dashboard
 * via the corresponding Jenkinsfile.
 *
 * @author Simon Symhoven
 */
public class Monitor extends Step implements Serializable {
    private static final long serialVersionUID = -1329798203887148860L;
    private String portlets;

    /**
     * Creates a new instance of {@link Monitor}.
     */
    @DataBoundConstructor
    public Monitor() {
        super();
        this.portlets = "[]";
    }

    /**
     * Sets the portlets for the dashboard.
     *
     * @param portlets
     *         the configuration as json
     */
    @DataBoundSetter
    public void setPortlets(final String portlets) {
        InputStream schemaStream = getClass().getResourceAsStream("/schema.json");
        JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaStream));
        JSONArray jsonSubject = new JSONArray(portlets);
        Schema schema = SchemaLoader.load(jsonSchema);
        schema.validate(jsonSubject);

        this.portlets = jsonSubject.toString();
    }

    public String getPortlets() {
        return portlets;
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
    public List<? extends MonitorPortlet> getAvailablePortlets(Run<?, ?> build) {
        return getFactories()
                .stream()
                .map(factory -> factory.getPortlets(build))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<? extends MonitorPortletFactory> getFactories() {
        return ExtensionList.lookup(MonitorPortletFactory.class);
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
    public List<? extends MonitorPortlet> getAvailablePortletsForFactory(
            Run<?, ?> build, MonitorPortletFactory factory) {
        return getFactories()
                .stream()
                .filter(fac -> fac.equals(factory))
                .map(fac -> fac.getPortlets(build))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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

        Execution(StepContext stepContext, Monitor monitor) {
            super(stepContext);
            this.monitor = monitor;
        }

        @Override
        public Void run() throws Exception {
            JSONArray portlets = new JSONArray(monitor.getPortlets());
            getContext().get(TaskListener.class).getLogger()
                    .println("[Monitor] Portlet Configuration: " + portlets.toString(3));

            List<String> classes = monitor.getAvailablePortlets(getContext().get(Run.class))
                    .stream()
                    .map(MonitorPortlet::getId)
                    .collect(Collectors.toList());

            getContext().get(TaskListener.class).getLogger()
                    .println("[Monitor] Available portlets: ["
                            + StringUtils.join(classes, ", ") + "]");

            List<String> usedPortlets = new ArrayList<>();

            for (Object o : portlets) {
                JSONObject portlet = (JSONObject) o;
                String id = portlet.getString("id");

                if (usedPortlets.contains(id)) {
                    getContext().get(TaskListener.class).getLogger()
                            .println("[Monitor] Portlet with ID '" + id
                                    + "' already defined in list of portlets. Skip adding this portlet!");
                }
                else {
                    usedPortlets.add(id);
                }
            }

            List<String> missedPortletIds = new ArrayList<String>(CollectionUtils.removeAll(usedPortlets, classes));

            if (missedPortletIds.size() > 0) {
                getContext().get(TaskListener.class).getLogger()
                        .println("[Monitor] Can't find the following portlets "
                                + missedPortletIds + " in list of available portlets! Will remove from current configuration.");

                JSONArray cleanedPortlets = new JSONArray();
                for (Object o : portlets) {
                    JSONObject portlet = (JSONObject) o;
                    if (!missedPortletIds.contains(portlet.getString("id"))) {
                        cleanedPortlets.put(portlet);
                    }
                }

                monitor.setPortlets(cleanedPortlets.toString(3));

                getContext().get(TaskListener.class).getLogger()
                        .println("[Monitor] Cleaned Portlets: " + cleanedPortlets.toString(3));
            }

            final Run<?, ?> run = getContext().get(Run.class);
            final Job<?, ?> job = run.getParent();
            final BranchJobProperty branchJobProperty = job.getProperty(BranchJobProperty.class);

            if (branchJobProperty != null) {
                final SCMHead head = branchJobProperty.getBranch().getHead();

                if (head instanceof ChangeRequestSCMHead) {
                    getContext().get(TaskListener.class).getLogger()
                            .println("[Monitor] Build is part of a pull request. Add 'MonitoringCustomAction' now.");

                    run.addAction(new MonitoringCustomAction(monitor));
                }
                else {
                    getContext().get(TaskListener.class).getLogger()
                            .println("[Monitor] Build is not part of a pull request. Skip adding 'MonitoringCustomAction'.");
                }
            }
            else {
                getContext().get(TaskListener.class).getLogger()
                        .println("[Monitor] Project is not a MultiBranchProject.");
            }

            return null;
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
            return "Configure Monitoring Dashboard";
        }
    }

}
