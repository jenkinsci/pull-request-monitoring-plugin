package io.jenkins.plugins.monitoring;

import hudson.Extension;
import hudson.model.Action;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@link TransientActionFactory} to add an action to specific {@link WorkflowJob}.
 *
 * @author Simon Symhoven
 */
@Extension
public class MonitoringWorkflowJobActionFactory extends TransientActionFactory<WorkflowJob> {

    /**
     * Specifies the {@link Class} of the job {@link WorkflowJob} to add the action to.
     *
     * @return
     *          the {@link Class} of job to add the action to.
     */
    @Override
    public Class<WorkflowJob> type() {
        return WorkflowJob.class;
    }

    /**
     * Add the action to the selected {@link WorkflowJob} if its a Pull Request.
     *
     * @param workflowJob
     *          the job to add the action to.
     * @return
     *          {@link Collections} of {@link MonitoringWorkflowJobAction} if
     *          {@link WorkflowJob} is a Pull Request, else a empty collection.
     */
    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull WorkflowJob workflowJob) {

        if (workflowJob.getPronoun().equals("Pull Request") ) {
            return Collections.singletonList(new MonitoringWorkflowJobAction(workflowJob));
        }

        return Collections.emptyList();
    }

}
