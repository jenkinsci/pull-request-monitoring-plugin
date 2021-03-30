package io.jenkins.plugins.monitoring;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.ProminentProjectAction;
import jenkins.branch.Branch;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This action displays a link on the side panel of a {@link MultiBranchProject}.
 * The action is responsible to render the basic overview of all open pull requests
 * via its associated 'index.jelly' view.
 *
 * @author Simon Symhoven
 */
public class MonitoringMultibranchProjectAction implements ProminentProjectAction, Action {
    static final String URI = "pull-request-monitoring";
    static final String DISPLAY_NAME = "Pull Request Monitoring";
    static final String ICONS_PREFIX = "/plugin/pull-request-monitoring/icons/";
    static final String ICON_SMALL = ICONS_PREFIX + "pull-request-monitoring-24x24.png";
    static final String ICON_BIG = ICONS_PREFIX + "pull-request-monitoring-48x48.png";
    private static final String DEFAULT_BRANCH = "master";
    private String defaultBranch = DEFAULT_BRANCH;

    private transient final MultiBranchProject<?, ?> multiBranchProject;

    public MonitoringMultibranchProjectAction(MultiBranchProject<?, ?> multiBranchProject) {
        this.multiBranchProject = multiBranchProject;
    }

    @Override
    public String getIconFileName() {
        return ICON_BIG;
    }


    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getUrlName() {
        return URI;
    }

    public MultiBranchProject<?, ?> getProject() {
        return multiBranchProject;
    }

    /**
     * Filters all jobs of selected {@link MultiBranchProject} by "Pull Request".
     *
     * @return
     *          filtered list of all {@link #getJobs() jobs} by "Pull Request".
     */
    public List<Job<?, ?>> getPullRequests() {
        return filterJobs("Pull Request");
    }

    /**
     * Filters all jobs of selected {@link MultiBranchProject} by "Branch".
     *
     * @return
     *          filtered list of all {@link #getJobs() jobs} by "Branch".
     */
    public List<Job<?, ?>> getBranches() {
        return filterJobs("Branch");
    }

    /**
     * Get the {@link BranchJobProperty} for a specific {@link Job}.
     *
     * @param job
     *          the job to get {@link Branch} for.
     * @return
     *          the {@link Branch} of job.
     */
    public Branch getBranch(Job<?, ?> job) {
        return job.getProperty(BranchJobProperty.class).getBranch();
    }

    /**
     * Get the {@link BranchJobProperty} for the reference branch.
     *
     * @return
     *          the reference {@link Branch}.
     */
    public Branch getTarget() {
        Job<?, ?> target = multiBranchProject.getItemByBranchName(getReferenceBranch());

        if (target == null) {
            return null;
        }

        return target.getProperty(BranchJobProperty.class).getBranch();
    }

    /**
     * Get the name of reference branch.
     *
     * @return
     *          the name of reference branch (default: master).
     */
    private String getReferenceBranch() {
        return StringUtils.defaultIfBlank(StringUtils.strip(defaultBranch), DEFAULT_BRANCH);
    }

    /**
     * Fetch all jobs (items) of current {@link MultiBranchProject}.
     *
     * @return
     *          {@link List} of all jobs of current {@link MultiBranchProject}.
     */
    private List<Job<?, ?>> getJobs() {
        return multiBranchProject.getItems().stream().map(item -> (Job<?, ?>) item).collect(Collectors.toList());
    }

    /**
     * Filters all jobs of selected {@link MultiBranchProject} by given pronoun.
     *
     * @param pronounToFilter
     *          the name of Job-Type, e.g. "Branch" or "Pull Request" to filter by.
     *
     * @return
     *          filtered list of all {@link #getJobs() jobs}.
     */
    private List<Job<?, ?>> filterJobs(String pronounToFilter) {
        return getJobs().stream().filter(job -> {
            BranchJobProperty branchJobProperty = job.getProperty(BranchJobProperty.class);
            String pronoun = branchJobProperty.getBranch().getHead().getPronoun();

            if (pronoun == null) {
                return false;
            }

            return pronoun.equals(pronounToFilter);

        }).collect(Collectors.toList());
    }

    /**
     *
     * @param job
     *          the job to get {@link ObjectMetadataAction} for.
     * @return
     *          the {@link ObjectMetadataAction} for the given job.
     */
    public ObjectMetadataAction getObjectMetaData(Job<?, ?> job) {
        Optional<Action> objectMetadataAction = job.getProperty(BranchJobProperty.class).getBranch().getActions()
                .stream().filter(action -> action instanceof ObjectMetadataAction).findFirst();

        return (ObjectMetadataAction) objectMetadataAction.orElse(null);
    }

}
