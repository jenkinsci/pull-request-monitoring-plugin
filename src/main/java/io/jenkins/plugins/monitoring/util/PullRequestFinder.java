package io.jenkins.plugins.monitoring.util;

import hudson.model.Job;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

/**
 * A utility class for pull requests.
 */
public final class PullRequestFinder {

    private PullRequestFinder() {
        // make checkstyle happy.
    }

    /**
     * Checks whether a given {@link Job} is a pull request or not.
     *
     * @param job
     *              the job to analyse.
     *
     * @return
     *              true if the job is a pull request, else false.
     */
    public static boolean isPullRequest(Job<?, ?> job) {
        BranchJobProperty branchJobProperty = job.getProperty(BranchJobProperty.class);
        SCMHead head = branchJobProperty.getBranch().getHead();
        return head instanceof ChangeRequestSCMHead2;
    }
}
