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

package com.mobilecashout.osprey.command;

import com.google.inject.Inject;
import com.mobilecashout.osprey.Osprey;
import com.mobilecashout.osprey.exception.BaseException;
import com.rfksystems.commander.Command;
import com.rfksystems.commander.Input;
import com.rfksystems.commander.exception.RuntimeArgumentException;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

public class DeployCommand implements Command {
    @Inject
    private Osprey osprey;
    @Inject
    private Logger logger;

    @Override
    public String getName() {
        return "deploy";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "<project>           : required, name of the project to deploy.",
                "<environment>       : required, name of environment to deploy to.",
                "[--config=file.json]: optional, configuration file path. Defaults to osprey.json.",
                "[--verbose]         : optional, display debug output. Note, that deployment log will always record all log output anyway. Defaults to false.",
                "[--assume-yes]      : optional, don't prompt for any confirmations. Defaults to false.",
                "[--debug]           : optional, pause after before execution step"
        };
    }

    @Override
    public String getDescription() {
        return "deploy given project to indicated environment";
    }

    @Override
    public int execute(Input input, PrintStream printStream) throws RuntimeArgumentException {
        if (input.positional.size() != 2) {
            throw new RuntimeArgumentException("Invalid number of arguments. See help.");
        }

        String config = "osprey.json";
        boolean verbose = false;
        boolean assumeYes = false;
        boolean debug = false;

        if (input.arguments.containsKey("config")) {
            config = input.arguments.get("config").getString();
        }

        if (input.arguments.containsKey("verbose")) {
            verbose = true;
        }

        if (input.arguments.containsKey("assume-yes")) {
            assumeYes = true;
        }

        if (input.arguments.containsKey("debug")) {
            debug = true;
        }

        try {
            osprey.deploy(
                    input.positional.get(0),
                    input.positional.get(1),
                    config,
                    verbose,
                    assumeYes,
                    debug
            );
        } catch (BaseException e) {
            if (verbose) {
                logger.fatal(e.getMessage(), e);
                return -1;
            }
            logger.fatal(e.getMessage());
            return -1;
        }
        return 0;
    }
}
