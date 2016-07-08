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

import com.mobilecashout.osprey.project.ProjectConfigurationError;

public class DeploymentPlanManager implements BuildPlanner {
    private final BuildPlanner[] buildPlanners;

    public DeploymentPlanManager(BuildPlanner[] buildPlanners) {
        this.buildPlanners = buildPlanners;
    }

    @Override
    public void plan(DeploymentPlan deploymentPlan, DeploymentContext context) throws ProjectConfigurationError {
        for (BuildPlanner planner : buildPlanners) {
            planner.plan(deploymentPlan, context);
        }
    }
}
