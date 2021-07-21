package io.jenkins.plugins.monitoring;

import hudson.model.Action;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

/**
 * This action displays a link on the side panel of a {@link WorkflowJob}. The action is only displayed if the job
 * is a pull request, which is described in the associated {@link MonitoringWorkflowJobActionFactory}.
 * The action is responsible to reference the latest build of the job and navigates to the corresponding
 * {@link MonitoringDefaultAction}.
 *
 * @author Simon Symhoven
 */
public class MonitoringWorkflowJobAction implements Action {

    private final transient WorkflowJob workflowJob;

    /**
     * Creates a new instance of {@link MonitoringWorkflowJobAction}.
     *
     * @param workflowJob
     *          the job that owns owns this action.
     */
    public MonitoringWorkflowJobAction(final WorkflowJob workflowJob) {
        this.workflowJob = workflowJob;
    }

    @Override
    public String getIconFileName() {
        return MonitoringMultibranchProjectAction.getIconSmall();
    }

    @Override
    public String getDisplayName() {
        return String.format("%s '%s'", Messages.JobAction_Name(), workflowJob.getLastBuild().getDisplayName());
    }

    @Override
    public String getUrlName() {
        return workflowJob.getLastBuild().getNumber() + "/" + MonitoringMultibranchProjectAction.getURI();
    }
}
