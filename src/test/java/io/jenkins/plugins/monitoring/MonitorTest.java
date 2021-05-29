package io.jenkins.plugins.monitoring;

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
     * Test if default monitor and {@link MonitoringDefaultAction} is added if {@link hudson.model.Run} is a pull request.
     */
    @Test
    public void shouldAddDefaultMonitorWhenBuildIsPr() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.default");

            project.scheduleBuild2(0);
            jenkinsRule.waitUntilNoActivity();

            final WorkflowJob job = project.getItems().iterator().next();
            final WorkflowRun build = job.getLastBuild();
            MonitoringCustomAction action = build.getAction(MonitoringCustomAction.class);

            jenkinsRule.assertBuildStatusSuccess(build);
            jenkinsRule.assertLogContains("[Monitor] Portlet Configuration: []", build);
            jenkinsRule.assertLogContains("[Monitor] Build is part of a pull request. Add 'MonitoringCustomAction' now.", build);
            Assert.assertNotNull(action);
            Assert.assertEquals(action.getMonitor().getPortlets(), "[]");
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Test if nothing is added if {@link hudson.model.Run} is not a pull request.
     */
    @Test
    public void shouldSkipAddDefaultMonitorWhenBuildIsNotPr() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithoutPr("Jenkinsfile.default");

            project.scheduleBuild2(0);
            jenkinsRule.waitUntilNoActivity();

            final WorkflowJob job = project.getItems().iterator().next();
            final WorkflowRun build = job.getLastBuild();
            MonitoringCustomAction action = build.getAction(MonitoringCustomAction.class);

            jenkinsRule.assertBuildStatusSuccess(build);
            jenkinsRule.assertLogContains("[Monitor] Build is not part of a pull request. Skip adding 'MonitoringCustomAction'.", build);
            Assert.assertNull(action);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Test if missed portlets are removed form monitor if unknown plugin id is used in monitoring configuration.
     */
    @Test
    public void shouldRemovePortletFromConfigurationWhenAddingNotExistingPortlet() {

        try {
            WorkflowMultiBranchProject project = createRepositoryWithPr("Jenkinsfile.custom2");

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
            Assert.assertEquals(action.getMonitor().getPortlets(), "[]");
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
