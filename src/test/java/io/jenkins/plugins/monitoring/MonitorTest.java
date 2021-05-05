package io.jenkins.plugins.monitoring;

import hudson.model.Result;
import jenkins.branch.BranchSource;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMDiscoverBranches;
import jenkins.scm.impl.mock.MockSCMDiscoverChangeRequests;
import jenkins.scm.impl.mock.MockSCMSource;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.io.InputStream;


/**
 * Unit tests for the {@link Monitor} step.
 *
 * @author simonsymhoven
 */
public class MonitorTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void should_AddDefaultMonitor_When_BuildIsPr() throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.default");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();
        MonitoringBuildAction action = build.getAction(MonitoringBuildAction.class);

        jenkinsRule.assertBuildStatusSuccess(build);
        jenkinsRule.assertLogContains("Build is part of a pull request. Add monitor now." , build);
        Assert.assertNotNull(action);
        Assert.assertEquals(action.getMonitor().getConfiguration(), "{\"plugins\": {}}");
    }

    @Test
    public void should_SkipAddDefaultMonitor_When_BuildIsNotPr() throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithoutPr("Jenkinsfile.default");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();
        MonitoringBuildAction action = build.getAction(MonitoringBuildAction.class);

        jenkinsRule.assertBuildStatusSuccess(build);
        jenkinsRule.assertLogContains("Build is not part of a pull request. Skip adding monitor." , build);
        Assert.assertNull(action);
    }

    @Test
    public void should_ThrowException_When_AddingNotExistingView() throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.custom2");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();
        MonitoringBuildAction action = build.getAction(MonitoringBuildAction.class);

        jenkinsRule.assertBuildStatus(Result.FAILURE, build);
        jenkinsRule.assertLogContains(
                "Can't find class 'io.jenkins.plugins.view' in list of available plugins!" , build);
        Assert.assertNull(action);
    }

    private WorkflowMultiBranchProject createRepositoryWithPr(String jenkinsfile) throws IOException {
        MockSCMController controller = MockSCMController.create();
        controller.createRepository("scm-repo");
        controller.createBranch("scm-repo", "master");
        final int num = controller.openChangeRequest("scm-repo", "master");
        final String crNum = "change-request/" + num;
        InputStream st = getClass().getResourceAsStream(String.format("/io/jenkins/plugins/monitoring/%s", jenkinsfile));
        byte[] targetArray = new byte[st.available()];
        st.read(targetArray);
        controller.addFile("scm-repo", crNum, "Jenkinsfile", "Jenkinsfile",
                targetArray);

        WorkflowMultiBranchProject project = jenkinsRule.createProject(WorkflowMultiBranchProject.class);
        project.getSourcesList().add(new BranchSource(new MockSCMSource(controller, "scm-repo",
                new MockSCMDiscoverChangeRequests())));


        return project;
    }

    private WorkflowMultiBranchProject createRepositoryWithoutPr(String jenkinsfile) throws IOException {
        MockSCMController controller = MockSCMController.create();
        controller.createRepository("scm-repo");
        controller.createBranch("scm-repo", "master");
        InputStream st = getClass().getResourceAsStream(String.format("/io/jenkins/plugins/monitoring/%s", jenkinsfile));
        byte[] targetArray = new byte[st.available()];
        st.read(targetArray);
        controller.addFile("scm-repo", "master", "Jenkinsfile", "Jenkinsfile",
                targetArray);

        WorkflowMultiBranchProject project = jenkinsRule.createProject(WorkflowMultiBranchProject.class);
        project.getSourcesList().add(new BranchSource(new MockSCMSource(controller, "scm-repo",
                new MockSCMDiscoverBranches())));

        return project;
    }
}
