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
import com.mobilecashout.osprey.deployer.DeploymentActionError;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.deployer.DeploymentPlan;
import com.mobilecashout.osprey.plugin.PluginInterface;
import org.apache.logging.log4j.Logger;

public class EchoPlugin implements PluginInterface {
    @Inject
    private Logger logger;

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public String[] getEnvironments() {
        return new String[]{
                PluginInterface.LOCAL,
                PluginInterface.REMOTE
        };
    }

    @Override
    public DeploymentAction[] actionFromCommand(String command, DeploymentPlan deploymentPlan, DeploymentContext deploymentContext) {
        return new DeploymentAction[]{
                new EchoAction(command)
        };
    }

    private class EchoAction implements DeploymentAction {
        private final String command;

        EchoAction(String command) {
            this.command = command;
        }

        @Override
        public String getDescription() {
            return "Echo a value to log: " + command;
        }

        @Override
        public void execute(DeploymentContext context) throws DeploymentActionError {
            logger.debug(context.substitutor().replace(command));
        }
    }
}
