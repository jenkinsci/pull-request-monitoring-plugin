package io.jenkins.plugins.monitoring;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import io.jenkins.cli.shaded.org.slf4j.Logger;
import io.jenkins.cli.shaded.org.slf4j.LoggerFactory;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Set;

public class Monitor extends Step implements Serializable {
    private static final long serialVersionUID = -1329798203887148860L;
    private String configuration;

    /**
     * Creates a new instance of {@link Monitor}.
     */
    @DataBoundConstructor
    public Monitor() {
        super();
        System.out.println("Init Monitor");
    }

    /**
     * Sets the configuration for the dashboard
     *
     * @param configuration
     *         the configuration as json
     */
    @DataBoundSetter
    public void setConfiguration(final String configuration) {
        System.out.println("Set config");
        this.configuration = configuration;
    }

    public String getConfiguration() {
        return configuration;
    }

    @Override
    public StepExecution start(final StepContext stepContext) throws Exception {
        return new Execution(stepContext, this);
    }

    static class Execution extends SynchronousStepExecution {
        private static final transient Logger LOGGER = LoggerFactory.getLogger( Execution.class.getName());
        private static final long serialVersionUID = 1300005476208035751L;
        private final Monitor monitor;

        public Execution(StepContext stepContext, Monitor monitor)
        {
            super(stepContext);
            this.monitor = monitor;
        }

        @Override
        protected Object run() throws Exception {
            LOGGER.info( "Run Monitor" );
            LOGGER.info("Config: " + monitor.getConfiguration());
            return null;
        }
    }

    @Extension
    @Symbol("monitor")
    public static class Descriptor extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of( FilePath.class, TaskListener.class, Launcher.class);
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
