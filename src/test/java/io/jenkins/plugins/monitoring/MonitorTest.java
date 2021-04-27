package io.jenkins.plugins.monitoring;

import hudson.triggers.SCMTrigger;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Unit tests for the {@link Monitor} step.
 *
 * @author simonsymhoven
 */
public class MonitorTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void should() throws Exception {
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "Pull Request");
        job.addTrigger(new SCMTrigger(""));
        job.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "   stage ('Pull Request Monitoring - Empty Dashboard Configuration') {" +
                "       monitoring ( )" +
                "   }" +
                "}", true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.assertLogContains("Configuration: {\"plugins\": {}}" , run);
    }
}
