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
import com.mobilecashout.osprey.deployer.actions.LocalShellAction;
import com.mobilecashout.osprey.plugin.PluginInterface;
import com.mobilecashout.osprey.deployer.DeploymentAction;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.deployer.DeploymentPlan;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.LogOutputStream;

public class LocalShellPlugin implements PluginInterface {
    @Inject
    private LogOutputStream logOutputStream;

    @Override
    public String getName() {
        return "shell";
    }

    @Override
    public String[] getEnvironments() {
        return new String[]{
                PluginInterface.LOCAL
        };
    }

    @Override
    public DeploymentAction[] actionFromCommand(String command, DeploymentPlan deploymentPlan, DeploymentContext deploymentContext) {
        return new DeploymentAction[]{
                new LocalShellAction(CommandLine.parse(command), logOutputStream)
        };
    }
}
