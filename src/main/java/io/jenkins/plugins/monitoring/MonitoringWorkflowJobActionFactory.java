package io.jenkins.plugins.monitoring;

import hudson.Extension;
import hudson.model.Action;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

@Extension
public class MonitoringWorkflowJobActionFactory extends TransientActionFactory<WorkflowJob> {
    @Override
    public Class<WorkflowJob> type() {
        return WorkflowJob.class;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull WorkflowJob workflowJob) {
        if (workflowJob.getPronoun().equals("Pull Request")) {
            return Collections.singletonList(new MonitoringWorkflowJobAction(workflowJob));
        }

        return Collections.emptyList();
    }
}
