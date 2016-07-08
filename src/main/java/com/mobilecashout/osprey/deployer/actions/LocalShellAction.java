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

package com.mobilecashout.osprey.deployer.actions;

import com.mobilecashout.osprey.deployer.DeploymentAction;
import com.mobilecashout.osprey.deployer.DeploymentActionError;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.IOException;

public class LocalShellAction implements DeploymentAction {

    private static DefaultExecutor executor = new DefaultExecutor();

    private final CommandLine commandLine;
    private final LogOutputStream logOutputStream;

    public LocalShellAction(CommandLine commandLine, LogOutputStream logOutputStream) {
        this.commandLine = commandLine;
        this.logOutputStream = logOutputStream;
    }

    @Override
    public String getDescription() {
        return String.format("Local shell command: %s", commandLine.toString());
    }

    @Override
    public void execute(DeploymentContext context) throws DeploymentActionError {
        executor.setWorkingDirectory(context.buildDirectory());
        executor.setStreamHandler(new PumpStreamHandler(logOutputStream));
        try {
            logOutputStream.write(String.format("Local shell invoked: %s\n", commandLine.toString()).getBytes());
            executor.execute(commandLine);
        } catch (IOException e) {
            throw new DeploymentActionError(e);
        }
    }
}
