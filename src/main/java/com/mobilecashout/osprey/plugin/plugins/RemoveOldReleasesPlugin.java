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

package com.mobilecashout.osprey.plugin.plugins;

import com.mobilecashout.osprey.deployer.DeploymentAction;
import com.mobilecashout.osprey.deployer.DeploymentActionError;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.deployer.DeploymentPlan;
import com.mobilecashout.osprey.plugin.PluginInterface;
import com.mobilecashout.osprey.plugin.RolesUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class RemoveOldReleasesPlugin implements PluginInterface {
    @Override
    public String getName() {
        return "removeAllExcept";
    }

    @Override
    public String[] getEnvironments() {
        return new String[]{
                PluginInterface.REMOTE
        };
    }

    @Override
    public DeploymentAction[] actionFromCommand(String command, DeploymentPlan deploymentPlan, DeploymentContext deploymentContext) throws DeploymentActionError {
        return new DeploymentAction[]{
                new RemoveOldReleasesAction(command)
        };
    }

    private class RemoveOldReleasesAction implements DeploymentAction {
        private final ImmutablePair<String, String[]> commandRolePair;

        RemoveOldReleasesAction(String command) {
            this.commandRolePair = RolesUtil.parseCommandRoles(command);
        }

        @Override
        public String getDescription() {
            return String.format("Remove all but %s releases on roles [%s]", commandRolePair.getLeft(), String.join(",", (CharSequence[]) commandRolePair.getRight()));
        }

        @Override
        public void execute(DeploymentContext context) throws DeploymentActionError {
            context
                    .remoteClient()
                    .execute(
                            String.format("cd {releases_root}; ls -tQ | tail -n+%s | xargs rm -rf", Integer.valueOf(commandRolePair.getLeft()) + 1),
                            context,
                            commandRolePair.getRight()
                    );
        }
    }
}
