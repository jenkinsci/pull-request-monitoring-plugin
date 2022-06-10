package io.jenkins.plugins.monitoring;

import hudson.model.Result;
import jenkins.branch.BranchSource;
import jenkins.scm.impl.mock.MockSCMController;
import jenkins.scm.impl.mock.MockSCMDiscoverBranches;
import jenkins.scm.impl.mock.MockSCMDiscoverChangeRequests;
import jenkins.scm.impl.mock.MockSCMSource;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Unit tests for the {@link Monitor} step.
 *
 * @author Simon Symhoven
 */
@SuppressWarnings("checkstyle:IllegalCatch")
public class MonitorTest {

    /**
     * JUnit rule to allow test cases to fire up a Jenkins instance.
     */
    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();

    private MockSCMController controller;

    /**
     * Creates a {@link MockSCMController} before each test.
     */
    @Before
    public void init() {
        controller = MockSCMController.create();
    }

    /**
     * Closes the {@link MockSCMController} after each test. Cleans the repository.
     */
    @After
    public void teardown() {
        controller.close();
    }

    /**
     * Test if {@link MonitoringDefaultAction} and {@link MonitoringCustomAction} is added if {@link hudson.model.Run}
     * is a pull request and pipeline is valid.
     */
    @Test
    public void shouldAddCustomAndDefaultMonitorWhenBuildIsPr() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.customEmpty");

            project.scheduleBuild2(0);
            jenkinsRule.waitUntilNoActivity();

            final WorkflowJob job = project.getItems().iterator().next();
            final WorkflowRun build = job.getLastBuild();
            MonitoringCustomAction customAction = build.getAction(MonitoringCustomAction.class);
            MonitoringDefaultAction defaultAction = build.getAction(MonitoringDefaultAction.class);

            jenkinsRule.assertBuildStatusSuccess(build);
            jenkinsRule.assertLogContains("[Monitor] Portlet Configuration: []", build);
            jenkinsRule.assertLogContains("[Monitor] Build is part of a pull request. Add 'MonitoringCustomAction' now.", build);
            Assert.assertNotNull(customAction);
            Assert.assertNotNull(defaultAction);
            Assert.assertEquals(customAction.getPortlets(), "[]");
            Assert.assertEquals(defaultAction.getPortlets(), "[]");
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Test if {@link MonitoringDefaultAction} isn't and {@link MonitoringCustomAction} is added if {@link hudson.model.Run}
     * is a pull request and pipeline is empty.
     */
    @Test
    public void shouldAddDefaultMonitorWhenBuildIsPr() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.emptyStage");

            project.scheduleBuild2(0);
            jenkinsRule.waitUntilNoActivity();

            final WorkflowJob job = project.getItems().iterator().next();
            final WorkflowRun build = job.getLastBuild();
            MonitoringCustomAction customAction = build.getAction(MonitoringCustomAction.class);
            MonitoringDefaultAction defaultAction = build.getAction(MonitoringDefaultAction.class);

            jenkinsRule.assertBuildStatusSuccess(build);

            Assert.assertNull(customAction);
            Assert.assertNotNull(defaultAction);
            Assert.assertEquals(defaultAction.getPortlets(), "[]");
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Test if nothing is added if {@link hudson.model.Run} is not a pull request.
     */
    @Test
    public void shouldSkipAddDefaultAndCustomMonitorWhenBuildIsNotPr() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithoutPr("Jenkinsfile.customEmpty");

            project.scheduleBuild2(0);
            jenkinsRule.waitUntilNoActivity();

            final WorkflowJob job = project.getItems().iterator().next();
            final WorkflowRun build = job.getLastBuild();

            MonitoringCustomAction customAction = build.getAction(MonitoringCustomAction.class);
            MonitoringDefaultAction defaultAction = build.getAction(MonitoringDefaultAction.class);

            jenkinsRule.assertBuildStatusSuccess(build);
            jenkinsRule.assertLogContains("[Monitor] Build is not part of a pull request. Skip adding 'MonitoringCustomAction'.", build);
            Assert.assertNull(customAction);
            Assert.assertNull(defaultAction);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Test if missed portlets are removed from monitor if unknown plugin id is used in monitoring configuration.
     */
    @Test
    public void shouldRemovePortletFromConfigurationWhenAddingNotExistingPortlet() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.custom");

            project.scheduleBuild2(0);
            jenkinsRule.waitUntilNoActivity();

            final WorkflowJob job = project.getItems().iterator().next();
            final WorkflowRun build = job.getLastBuild();
            MonitoringCustomAction action = build.getAction(MonitoringCustomAction.class);

            jenkinsRule.assertBuildStatusSuccess(build);
            jenkinsRule.assertLogContains(
                    "[Monitor] Can't find the following portlets [io.jenkins.plugins.view] in list of available portlets!", build);
            jenkinsRule.assertLogContains(
                    "[Monitor] Cleaned Portlets: []", build);
            Assert.assertEquals(action.getPortlets(), "[]");

        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Test that run fails, if invalid configuration is provided in pipeline.
     */
    @Test
    public void shouldFailIfConfigurationInPipelineIsInvalid() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.error");

            project.scheduleBuild2(0);
            jenkinsRule.waitUntilNoActivity();

            final WorkflowJob job = project.getItems().iterator().next();
            final WorkflowRun build = job.getLastBuild();

            MonitoringCustomAction action = build.getAction(MonitoringCustomAction.class);

            jenkinsRule.assertBuildStatus(Result.FAILURE, build);
            Assert.assertNull(action);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Test that run fails, if no configuration is provided in pipeline.
     */
    @Test
    public void shouldFailIfNoConfigurationInPipelineIsProvided() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.error2");

            project.scheduleBuild2(0);
            jenkinsRule.waitUntilNoActivity();

            final WorkflowJob job = project.getItems().iterator().next();
            final WorkflowRun build = job.getLastBuild();

            MonitoringCustomAction action = build.getAction(MonitoringCustomAction.class);
            jenkinsRule.assertBuildStatus(Result.FAILURE, build);
            Assert.assertNull(action);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }
    
    /**
     * Creates an {@link WorkflowMultiBranchProject} with a sample pull request based on the given Jenkinsfile.
     *
     * @param jenkinsfile
     *              the name of the Jenkinsfile to add to the repository.
     *
     * @return
     *              the generated {@link WorkflowMultiBranchProject}.
     *
     */
    private WorkflowMultiBranchProject createRepositoryWithPr(final String jenkinsfile) {

        try (InputStream st = getClass().getResourceAsStream(String.format("/io/jenkins/plugins/monitoring/%s", jenkinsfile))) {

            controller.createRepository("scm-repo");
            controller.createBranch("scm-repo", "master");
            final int num = controller.openChangeRequest("scm-repo", "master");
            final String crNum = "change-request/" + num;

            byte[] targetArray = IOUtils.toByteArray(Objects.requireNonNull(st));

            controller.addFile("scm-repo", crNum, "Jenkinsfile", "Jenkinsfile",
                    targetArray);

            WorkflowMultiBranchProject project = jenkinsRule.createProject(WorkflowMultiBranchProject.class);
            project.getSourcesList().add(new BranchSource(new MockSCMSource(controller, "scm-repo",
                    new MockSCMDiscoverChangeRequests())));

            return project;
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Creates an {@link WorkflowMultiBranchProject} without a sample pull request based on the given Jenkinsfile.
     *
     * @param jenkinsfile
     *              the name of the Jenkinsfile to add to the repository.
     *
     * @return
     *              the generated {@link WorkflowMultiBranchProject}.
     *
     */
    private WorkflowMultiBranchProject createRepositoryWithoutPr(final String jenkinsfile) {

        try (InputStream st = getClass().getResourceAsStream(String.format("/io/jenkins/plugins/monitoring/%s", jenkinsfile))) {

            controller.createRepository("scm-repo");
            controller.createBranch("scm-repo", "master");

            byte[] targetArray = IOUtils.toByteArray(Objects.requireNonNull(st));

            controller.addFile("scm-repo", "master", "Jenkinsfile", "Jenkinsfile",
                    targetArray);

            WorkflowMultiBranchProject project = jenkinsRule.createProject(WorkflowMultiBranchProject.class);
            project.getSourcesList().add(new BranchSource(new MockSCMSource(controller, "scm-repo",
                    new MockSCMDiscoverBranches())));

            return project;
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

    }

}
