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

package com.mobilecashout.osprey;

import com.google.inject.Inject;
import com.mobilecashout.osprey.project.config.Project;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.deployer.DeploymentManager;
import com.mobilecashout.osprey.exception.BaseException;
import com.mobilecashout.osprey.project.ProjectCollectionLoader;
import com.mobilecashout.osprey.project.ProjectConfigurationError;
import com.mobilecashout.osprey.project.config.Environment;
import com.mobilecashout.osprey.project.config.ProjectCollection;
import com.mobilecashout.osprey.remote.RemoteClient;
import com.mobilecashout.osprey.remote.RemoteClientException;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;

public class Osprey {
    @Inject
    private Logger logger;
    @Inject
    private ProjectCollectionLoader loader;
    @Inject
    private DeploymentManager deploymentManager;

    public void deploy(String projectName, String environmentName, String configFileName, boolean verbose, boolean assumeYes, boolean debug) throws BaseException, RemoteClientException {
        logger.info(
                "Going to deploy {} to {} using configuration file {}",
                projectName,
                environmentName,
                configFileName
        );

        try {
            ProjectCollection projectCollection = loader.loadFromFile(configFileName);
            if (!projectCollection.contains(projectName)) {
                throw new ProjectConfigurationError(String.format("Project named %s could not be found in configuration %s", projectName, configFileName));
            }

            Project project = projectCollection.get(projectName);

            if (!project.environments().containsKey(environmentName)) {
                throw new ProjectConfigurationError(String.format("No environment %s defined in project %s in file %s", environmentName, projectName, configFileName));
            }

            Environment environment = project.environments().get(environmentName);

            DeploymentContext context = new DeploymentContext(
                    project,
                    environment,
                    environmentName,
                    verbose,
                    assumeYes,
                    debug
            );

            RemoteClient remoteClient = new RemoteClient(environment, context, logger);

            context.remoteClient(remoteClient);

            try {
                deploymentManager.deploy(context);
            } catch (BaseException e) {
                remoteClient.tryDisconnect();
                throw e;
            }

        } catch (FileNotFoundException e) {
            throw new ProjectConfigurationError(String.format("Configuration file %s not found", configFileName));
        }
    }
}
