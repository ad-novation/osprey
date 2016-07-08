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

package com.mobilecashout.osprey.deployer;

import com.google.inject.Inject;
import com.mobilecashout.osprey.project.ProjectConfigurationError;
import com.mobilecashout.osprey.util.Prompt;
import org.apache.logging.log4j.Logger;

public class DeploymentManager {
    @Inject
    private Logger logger;
    @Inject
    private DeploymentPlanManager deploymentPlanManager;

    public void deploy(DeploymentContext context) throws ProjectConfigurationError {
        logger.info("Current release will be {}", context.releaseId());

        logger.info("Preparing deployment plan...");

        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlanManager.plan(deploymentPlan, context);
        context.substitutor().list(logger);
        deploymentPlan.describe(logger);

        if (!context.isNonInteractive()) {
            boolean confirm = Prompt.confirm("Do you want to proceed?");
            if (!confirm) {
                logger.fatal("Deployment cancelled!");
                return;
            }
            logger.info("User confirmed input, executing deployment");
        } else {
            logger.warn("Deploying in non-interactive mode, no user confirmation requested");
        }

        deploymentPlan.executeInOrder(context, logger);

        if (context.isFailed()) {
            logger.fatal("Deployment failed!");
        } else {
            logger.info("Deployment success!");
        }
    }
}
