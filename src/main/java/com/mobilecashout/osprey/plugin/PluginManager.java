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

package com.mobilecashout.osprey.plugin;

import com.mobilecashout.osprey.deployer.DeploymentAction;
import com.mobilecashout.osprey.deployer.DeploymentActionError;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.deployer.DeploymentPlan;
import com.mobilecashout.osprey.project.ProjectConfigurationError;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Arrays;

public class PluginManager {
    private ArrayList<PluginInterface> plugins = new ArrayList<>();

    public void add(PluginInterface plugin) {
        plugins.add(plugin);
    }

    public void add(PluginInterface... plugins) {
        for (PluginInterface plugin : plugins) {
            add(plugin);
        }
    }

    public DeploymentAction[] actionsFromCommand(String command, DeploymentPlan deploymentPlan, String localRemote, DeploymentContext context) throws ProjectConfigurationError {
        if (null == command) {
            return new DeploymentAction[0];
        }
        ImmutablePair<String, PluginInterface> resolvedPair = null;
        try {
            resolvedPair = resolve(command, localRemote);
        } catch (IncompatibleEnvironmentError incompatibleEnvironmentError) {
            throw new ProjectConfigurationError(String.format(
                    "Plugin %s is not available for execution on %s environment",
                    incompatibleEnvironmentError.getHashName(),
                    incompatibleEnvironmentError.getEnvironment()
            ));
        }
        if (null == resolvedPair) {
            throw new ProjectConfigurationError(String.format("Plugin able to handle command <%s> in environment <%s> was not found", command, localRemote));
        }
        try {
            return resolvedPair.getRight().actionFromCommand(resolvedPair.getLeft(), deploymentPlan, context);
        } catch (DeploymentActionError deploymentActionError) {
            throw new ProjectConfigurationError(deploymentActionError);
        }
    }

    private ImmutablePair<String, PluginInterface> resolve(String command, String localRemote) throws IncompatibleEnvironmentError {
        String incompatibleFound = null;

        for (PluginInterface plugin : plugins) {
            String hashName = "@" + plugin.getName();
            if (command.startsWith(hashName)) {

                if (!Arrays.asList(plugin.getEnvironments()).contains(localRemote)) {
                    incompatibleFound = hashName;
                    continue;
                }

                String commandPart = "";
                if (command.length() > hashName.length() + 1) {
                    commandPart = command.substring(hashName.length() + 1);
                }

                return new ImmutablePair<>(
                        commandPart,
                        plugin
                );
            }
        }

        if (null != incompatibleFound) {
            throw new IncompatibleEnvironmentError(incompatibleFound, localRemote);
        }

        return null;
    }
}
