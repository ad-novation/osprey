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

package com.mobilecashout.osprey.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mobilecashout.osprey.command.DeployCommand;
import com.mobilecashout.osprey.command.InitCommand;
import com.rfksystems.commander.Commander;

public class CommanderProvider implements Provider<Commander> {
    @Inject
    private DeployCommand deployCommand;
    @Inject
    private InitCommand initCommand;

    @Override
    public Commander get() {
        Commander commander = new Commander();
        commander.setAppDescription("Osprey Deployment Tool");
        commander.addCommand(deployCommand);
        commander.addCommand(initCommand);
        return commander;
    }
}
