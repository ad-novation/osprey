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

import com.google.inject.Inject;
import com.mobilecashout.osprey.deployer.DeploymentAction;
import com.mobilecashout.osprey.deployer.actions.LocalShellAction;
import com.mobilecashout.osprey.plugin.PluginInterface;
import com.mobilecashout.osprey.util.Substitutor;
import com.mobilecashout.osprey.deployer.DeploymentActionError;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.deployer.DeploymentPlan;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.LogOutputStream;

public class ClonePlugin implements PluginInterface {
    @Inject
    private LogOutputStream logOutputStream;

    @Override
    public String getName() {
        return "clone";
    }

    @Override
    public String[] getEnvironments() {
        return new String[]{
                PluginInterface.LOCAL
        };
    }

    @Override
    public DeploymentAction[] actionFromCommand(String command, DeploymentPlan deploymentPlan, DeploymentContext deploymentContext) throws DeploymentActionError {
        return new DeploymentAction[]{
                new CloneAction(command, deploymentContext.substitutor())
        };
    }

    private class CloneAction implements DeploymentAction {
        private String repository;
        private String branch;

        CloneAction(String command, Substitutor substitutor) throws DeploymentActionError {
            String[] commandParts = command.split("\\s");
            if (commandParts.length != 2) {
                throw new DeploymentActionError("Clone command accepts 2 arguments: repository and branch");
            }
            repository = substitutor.replace(commandParts[0]);
            branch = substitutor.replace(commandParts[1]);
        }

        @Override
        public String getDescription() {
            return String.format("Clone %s:%s in local build directory", repository, branch);
        }

        @Override
        public void execute(DeploymentContext context) throws DeploymentActionError {
            if (!context.buildDirectory().exists()) {
                context.buildDirectory().mkdir();
            }
            new LocalShellAction(CommandLine.parse(String.format(
                    "git clone --depth=1  --branch=\"%s\" --recursive --single-branch \"%s\" \"%s\"",
                    branch,
                    repository,
                    context.buildDirectory()
            )), logOutputStream).execute(context);
        }
    }
}
