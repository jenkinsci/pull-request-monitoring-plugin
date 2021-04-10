package io.jenkins.plugins.monitoring;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Set;

/**
 * This {@link Step} is responsible for the configuration of the monitoring dashboard
 * via the corresponding Jenkinsfile.
 *
 * @author Simon Symhoven
 */
public class Monitor extends Step implements Serializable {
    private static final long serialVersionUID = -1329798203887148860L;
    private String configuration;

    /**
     * Creates a new instance of {@link Monitor}.
     */
    @DataBoundConstructor
    public Monitor() {
        super();
    }

    /**
     * Sets the configuration for the dashboard
     *
     * @param configuration
     *         the configuration as json
     */
    @DataBoundSetter
    public void setConfiguration(final String configuration) {
        this.configuration = configuration;
    }

    public String getConfiguration() {
        return configuration;
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

        public Execution(StepContext stepContext, Monitor monitor) {
            super(stepContext);
            this.monitor = monitor;
        }

        @Override
        public Void run() throws Exception {
            final Run<?, ?> run = getContext().get(Run.class);
            if (run.getParent().getPronoun().equals("Pull Request")) {
                run.addAction(new MonitoringBuildAction(run, monitor));
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
        @Nonnull
        public String getDisplayName() {
            return "Configure Monitoring Dashboard";
        }
    }
}
