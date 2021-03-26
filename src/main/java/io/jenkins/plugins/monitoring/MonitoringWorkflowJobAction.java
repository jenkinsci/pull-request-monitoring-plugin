package io.jenkins.plugins.monitoring;

import hudson.model.Action;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.File;

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
