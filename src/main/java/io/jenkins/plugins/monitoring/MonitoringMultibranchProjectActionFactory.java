package io.jenkins.plugins.monitoring;

import hudson.Extension;
import hudson.model.*;
import jenkins.branch.MultiBranchProject;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

@Extension
public class MonitoringMultibranchProjectActionFactory extends TransientActionFactory<MultiBranchProject> {

    @Override
    public Class<MultiBranchProject> type() {
        return MultiBranchProject.class;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull MultiBranchProject multiBranchProject) {
        return Collections.singletonList(new MonitoringMultibranchProjectAction(multiBranchProject));
    }
}
