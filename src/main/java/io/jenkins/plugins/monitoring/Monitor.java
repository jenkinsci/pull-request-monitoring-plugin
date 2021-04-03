package io.jenkins.plugins.monitoring;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    static class Execution extends StepExecution {
        private static final long serialVersionUID = 1300005476208035751L;
        private static final transient Logger LOGGER = Logger.getLogger(Execution.class.getName());
        private final transient TaskListener listener;
        private final Monitor monitor;

        public Execution(StepContext stepContext, Monitor monitor) throws IOException, InterruptedException {
            super(stepContext);
            this.monitor = monitor;
            this.listener = stepContext.get(TaskListener.class);
        }

        @Override
        public boolean start() throws Exception {
            Run<?, ?> build = getContext().get(Run.class);
            PrintStream console = listener.getLogger();
            MonitoringBuildAction action = new MonitoringBuildAction(build);

            action.setConfig("Test");
            build.addAction(action);


            LOGGER.log(Level.INFO, "Execution initialized.");
            console.println("Execution initialized.");
            return true;
        }
    }

    @Extension
    @Symbol("monitor")
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
