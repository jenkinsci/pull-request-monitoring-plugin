package io.jenkins.plugins.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import hudson.model.Result;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import jenkins.branch.BranchSource;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMDiscoverBranches;
import jenkins.scm.impl.mock.MockSCMDiscoverChangeRequests;
import jenkins.scm.impl.mock.MockSCMSource;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
@SuppressWarnings("checkstyle:IllegalCatch")
public class MonitorTest {

    private MockSCMController controller;

    /**
     * Creates a MockSCMController before each test.
     */
    @BeforeEach
    public void init() {
        controller = MockSCMController.create();
    }

    /**
     * Closes the MockSCMController after each test. Cleans the repository.
     */
    @AfterEach
    public void teardown() {
        controller.close();
    }

    /**
     * Test if MonitoringDefaultAction and MonitoringCustomAction are added
     * if the Run is a pull request and the pipeline is valid.
     */
    @Test
    public void shouldAddCustomAndDefaultMonitorWhenBuildIsPr(JenkinsRule jenkinsRule) throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithPr(jenkinsRule, "Jenkinsfile.customEmpty");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();

        MonitoringCustomAction customAction = build.getAction(MonitoringCustomAction.class);
        MonitoringDefaultAction defaultAction = build.getAction(MonitoringDefaultAction.class);

        jenkinsRule.assertBuildStatusSuccess(build);
        jenkinsRule.assertLogContains("[Monitor] Portlet Configuration: []", build);
        jenkinsRule.assertLogContains(
                "[Monitor] Build is part of a pull request. Add 'MonitoringCustomAction' now.", build);

        assertNotNull(customAction);
        assertNotNull(defaultAction);
        assertEquals("[]", customAction.getPortlets());
        assertEquals("[]", defaultAction.getPortlets());
    }

    /**
     * Test if MonitoringDefaultAction is added and MonitoringCustomAction
     * isn't added if the Run is a pull request and the pipeline is empty.
     */
    @Test
    public void shouldAddDefaultMonitorWhenBuildIsPr(JenkinsRule jenkinsRule) throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithPr(jenkinsRule, "Jenkinsfile.emptyStage");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();

        MonitoringCustomAction customAction = build.getAction(MonitoringCustomAction.class);
        MonitoringDefaultAction defaultAction = build.getAction(MonitoringDefaultAction.class);

        jenkinsRule.assertBuildStatusSuccess(build);

        assertNull(customAction);
        assertNotNull(defaultAction);
        assertEquals("[]", defaultAction.getPortlets());
    }

    /**
     * Test if nothing is added if the Run is not a pull request.
     */
    @Test
    public void shouldSkipAddDefaultAndCustomMonitorWhenBuildIsNotPr(JenkinsRule jenkinsRule) throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithoutPr(jenkinsRule, "Jenkinsfile.customEmpty");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();

        MonitoringCustomAction customAction = build.getAction(MonitoringCustomAction.class);
        MonitoringDefaultAction defaultAction = build.getAction(MonitoringDefaultAction.class);

        jenkinsRule.assertBuildStatusSuccess(build);
        jenkinsRule.assertLogContains(
                "[Monitor] Build is not part of a pull request. Skip adding 'MonitoringCustomAction'.", build);

        assertNull(customAction);
        assertNull(defaultAction);
    }

    /**
     * Test if missing portlets are removed from the monitor
     * if an unknown plugin ID is used in the monitoring configuration.
     */
    @Test
    public void shouldRemovePortletFromConfigurationWhenAddingNotExistingPortlet(JenkinsRule jenkinsRule)
            throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithPr(jenkinsRule, "Jenkinsfile.custom");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();

        MonitoringCustomAction action = build.getAction(MonitoringCustomAction.class);

        jenkinsRule.assertBuildStatusSuccess(build);
        jenkinsRule.assertLogContains(
                "[Monitor] Can't find the following portlets [io.jenkins.plugins.view] in list of available portlets!",
                build);
        jenkinsRule.assertLogContains("[Monitor] Cleaned Portlets: []", build);

        assertEquals("[]", action.getPortlets());
    }

    /**
     * Test that the run fails if an invalid configuration is provided in the pipeline.
     */
    @Test
    public void shouldFailIfConfigurationInPipelineIsInvalid(JenkinsRule jenkinsRule) throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithPr(jenkinsRule, "Jenkinsfile.error");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();

        MonitoringCustomAction action = build.getAction(MonitoringCustomAction.class);

        jenkinsRule.assertBuildStatus(Result.FAILURE, build);
        assertNull(action);
    }

    /**
     * Test that the run fails if no configuration is provided in the pipeline.
     */
    @Test
    public void shouldFailIfNoConfigurationInPipelineIsProvided(JenkinsRule jenkinsRule) throws Exception {
        WorkflowMultiBranchProject project = createRepositoryWithPr(jenkinsRule, "Jenkinsfile.error2");

        project.scheduleBuild2(0);
        jenkinsRule.waitUntilNoActivity();

        final WorkflowJob job = project.getItems().iterator().next();
        final WorkflowRun build = job.getLastBuild();

        MonitoringCustomAction action = build.getAction(MonitoringCustomAction.class);

        jenkinsRule.assertBuildStatus(Result.FAILURE, build);
        assertNull(action);
    }

    /**
     * Creates a WorkflowMultiBranchProject with a sample pull request
     * based on the given Jenkinsfile.
     *
     * @param jenkinsRule Jenkins test rule.
     * @param jenkinsfile the name of the Jenkinsfile to add to the repository.
     * @return the generated WorkflowMultiBranchProject.
     */
    private WorkflowMultiBranchProject createRepositoryWithPr(JenkinsRule jenkinsRule, final String jenkinsfile) {

        try (InputStream st =
                getClass().getResourceAsStream(String.format("/io/jenkins/plugins/monitoring/%s", jenkinsfile))) {

            controller.createRepository("scm-repo");
            controller.createBranch("scm-repo", "master");

            final int num = controller.openChangeRequest("scm-repo", "master");
            final String crNum = "change-request/" + num;

            byte[] targetArray = IOUtils.toByteArray(Objects.requireNonNull(st));

            controller.addFile("scm-repo", crNum, "Jenkinsfile", "Jenkinsfile", targetArray);

            WorkflowMultiBranchProject project = jenkinsRule.createProject(WorkflowMultiBranchProject.class);

            project.getSourcesList()
                    .add(new BranchSource(
                            new MockSCMSource(controller, "scm-repo", new MockSCMDiscoverChangeRequests())));

            return project;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a WorkflowMultiBranchProject without a sample pull request
     * based on the given Jenkinsfile.
     *
     * @param jenkinsRule Jenkins test rule.
     * @param jenkinsfile the name of the Jenkinsfile to add to the repository.
     * @return the generated WorkflowMultiBranchProject.
     */
    private WorkflowMultiBranchProject createRepositoryWithoutPr(JenkinsRule jenkinsRule, final String jenkinsfile) {

        try (InputStream st =
                getClass().getResourceAsStream(String.format("/io/jenkins/plugins/monitoring/%s", jenkinsfile))) {

            controller.createRepository("scm-repo");
            controller.createBranch("scm-repo", "master");

            byte[] targetArray = IOUtils.toByteArray(Objects.requireNonNull(st));

            controller.addFile("scm-repo", "master", "Jenkinsfile", "Jenkinsfile", targetArray);

            WorkflowMultiBranchProject project = jenkinsRule.createProject(WorkflowMultiBranchProject.class);

            project.getSourcesList()
                    .add(new BranchSource(new MockSCMSource(controller, "scm-repo", new MockSCMDiscoverBranches())));

            return project;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
