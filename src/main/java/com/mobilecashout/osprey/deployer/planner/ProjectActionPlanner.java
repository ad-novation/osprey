/*
 * Copyright 2016 Innovative Mobile Solutions Limited and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilecashout.osprey.deployer.planner;

import com.google.inject.Inject;
import com.mobilecashout.osprey.deployer.BuildPlanner;
import com.mobilecashout.osprey.plugin.PluginInterface;
import com.mobilecashout.osprey.project.config.BuildCommand;
import com.mobilecashout.osprey.project.config.BuildRoot;
import com.mobilecashout.osprey.deployer.DeploymentAction;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.deployer.DeploymentPlan;
import com.mobilecashout.osprey.plugin.PluginManager;
import com.mobilecashout.osprey.project.ProjectConfigurationError;

import java.util.ArrayList;
import java.util.Collections;

public class ProjectActionPlanner implements BuildPlanner {
    @Inject
    private PluginManager pluginManager;

    @Override
    public void plan(DeploymentPlan deploymentPlan, DeploymentContext context) throws ProjectConfigurationError {
        BuildRoot buildRoot = context.project().buildRoot();

        addCommandsFrom(buildRoot.failureCommands(), deploymentPlan.getGlobalFailure(), deploymentPlan, PluginInterface.LOCAL, context);
        addCommandsFrom(buildRoot.successCommands(), deploymentPlan.getGlobalSuccess(), deploymentPlan, PluginInterface.LOCAL, context);

        BuildCommand localBuild = buildRoot.localBuild();
        BuildCommand remoteBuild = buildRoot.remoteBuild();

        addCommandsFrom(localBuild.prepareCommands(), deploymentPlan.getLocalPrepare(), deploymentPlan, PluginInterface.LOCAL, context);
        addCommandsFrom(localBuild.successCommands(), deploymentPlan.getLocalSuccess(), deploymentPlan, PluginInterface.LOCAL, context);
        addCommandsFrom(localBuild.failureCommands(), deploymentPlan.getLocalFailure(), deploymentPlan, PluginInterface.LOCAL, context);

        addCommandsFrom(remoteBuild.prepareCommands(), deploymentPlan.getRemotePrepare(), deploymentPlan, PluginInterface.REMOTE, context);
        addCommandsFrom(remoteBuild.successCommands(), deploymentPlan.getRemoteSuccess(), deploymentPlan, PluginInterface.REMOTE, context);
        addCommandsFrom(remoteBuild.failureCommands(), deploymentPlan.getRemoteFailure(), deploymentPlan, PluginInterface.REMOTE, context);
    }

    private void addCommandsFrom(ArrayList<String> commandStrings, ArrayList<DeploymentAction> actionsCollection, DeploymentPlan deploymentPlan, String localRemote, DeploymentContext context) throws ProjectConfigurationError {
        for (String command : commandStrings) {
            DeploymentAction[] deploymentActions = pluginManager.actionsFromCommand(command, deploymentPlan, localRemote, context);
            Collections.addAll(actionsCollection, deploymentActions);
        }
    }
}
