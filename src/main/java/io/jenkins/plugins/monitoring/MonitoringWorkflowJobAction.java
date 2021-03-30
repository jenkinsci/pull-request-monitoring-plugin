package io.jenkins.plugins.monitoring;

import hudson.model.Action;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.File;

/**
 * This action displays a link on the side panel of a {@link WorkflowJob}. The action is only displayed if the job
 * is a pull request, which is described in the associated {@link MonitoringWorkflowJobActionFactory}.
 * The action is responsible to reference the latest build of the job and navigates to the corresponding
 * {@link MonitoringBuildAction}.
 *
 * @author Simon Symhoven
 */
public class MonitoringWorkflowJobAction implements Action {

    private transient WorkflowJob workflowJob;

    public MonitoringWorkflowJobAction(WorkflowJob workflowJob) {
        this.workflowJob = workflowJob;
    }

    @Override
    public String getIconFileName() {
        return MonitoringMultibranchProjectAction.ICON_SMALL;
    }

    @Override
    public String getDisplayName() {
        return String.format("%s '%s'", MonitoringMultibranchProjectAction.DISPLAY_NAME, workflowJob.getLastBuild().getDisplayName());
    }

    @Override
    public String getUrlName() {
        return workflowJob.getLastBuild().getNumber() + File.separator + MonitoringMultibranchProjectAction.URI;
    }

}
