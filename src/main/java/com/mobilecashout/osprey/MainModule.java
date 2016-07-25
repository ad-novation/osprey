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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.mobilecashout.osprey.command.DeployCommand;
import com.mobilecashout.osprey.deployer.DeploymentPlanManager;
import com.mobilecashout.osprey.deployer.planner.ProjectActionPlanner;
import com.mobilecashout.osprey.logger.LogOutputStreamImpl;
import com.mobilecashout.osprey.plugin.PluginManager;
import com.mobilecashout.osprey.provider.CommanderProvider;
import com.mobilecashout.osprey.provider.DeploymentPlanManagerProvider;
import com.mobilecashout.osprey.provider.LoggerProvider;
import com.mobilecashout.osprey.provider.PluginManagerProvider;
import com.rfksystems.commander.Commander;
import org.apache.commons.exec.LogOutputStream;
import org.apache.logging.log4j.Logger;

public class MainModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(Osprey.class).in(Singleton.class);
        binder.bind(Commander.class).toProvider(CommanderProvider.class).in(Singleton.class);
        binder.bind(DeployCommand.class).in(Singleton.class);
        binder.bind(Logger.class).toProvider(LoggerProvider.class).in(Singleton.class);
        binder.bind(DeploymentPlanManager.class).toProvider(DeploymentPlanManagerProvider.class).in(Singleton.class);
        binder.bind(LogOutputStream.class).to(LogOutputStreamImpl.class).in(Singleton.class);
        binder.bind(PluginManager.class).toProvider(PluginManagerProvider.class).in(Singleton.class);
        binder.bind(ProjectActionPlanner.class).in(Singleton.class);
    }
}
