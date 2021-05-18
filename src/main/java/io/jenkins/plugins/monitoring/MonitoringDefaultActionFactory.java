package io.jenkins.plugins.monitoring;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.Action;
import hudson.model.Job;
import jenkins.model.TransientActionFactory;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestSCMHead;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.Collection;
import java.util.Collections;

/**
 * A {@link TransientActionFactory} to add an action to specific {@link Run}.
 *
 * @author Simon Symhoven
 */
@Extension
public class MonitoringDefaultActionFactory extends TransientActionFactory<Run> {
    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull Run run) {

        final Job<?, ?> job = run.getParent();
        final BranchJobProperty branchJobProperty = job.getProperty(BranchJobProperty.class);

        if (branchJobProperty != null) {
            final SCMHead head = branchJobProperty.getBranch().getHead();

            if (head instanceof ChangeRequestSCMHead) {
                return Collections.singletonList(new MonitoringDefaultAction(run, new Monitor()));
            }
        }

        return Collections.emptyList();
    }
}
