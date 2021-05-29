package io.jenkins.plugins.monitoring;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Action;
import jenkins.branch.MultiBranchProject;
import jenkins.model.TransientActionFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * A {@link TransientActionFactory} to add an action to specific {@link MultiBranchProject}.
 *
 * @author Simon Symhoven
 */
@Extension
public class MonitoringMultibranchProjectActionFactory extends TransientActionFactory<MultiBranchProject> {

    /**
     * Specifies the {@link Class} of the job ({@link MultiBranchProject}) to add the action to.
     *
     * @return
     *          the {@link Class} of the job to add the action to.
     */
    @Override
    public Class<MultiBranchProject> type() {
        return MultiBranchProject.class;
    }

    /**
     * Add the action to the selected {@link MultiBranchProject}.
     *
     * @param multiBranchProject
     *          the job to add the action to.
     * @return
     *          {@link Collections} of {@link MonitoringMultibranchProjectAction}.
     */
    @Override
    @NonNull
    public Collection<? extends Action> createFor(@NonNull final MultiBranchProject multiBranchProject) {
        return Collections.singletonList(new MonitoringMultibranchProjectAction(multiBranchProject));
    }

}
