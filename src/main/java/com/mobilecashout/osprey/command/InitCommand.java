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
import com.rfksystems.commander.Command;
import com.rfksystems.commander.Input;
import com.rfksystems.commander.exception.RuntimeArgumentException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class InitCommand implements Command {
    @Inject
    private Logger logger;

    @Override
    public String getName() {
        return "init";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Create a blank osprey.json file in current working directory";
    }

    @Override
    public int execute(Input input, PrintStream printStream) throws RuntimeArgumentException {
        InputStream blankJsonStream = this.getClass().getClassLoader().getResourceAsStream("blank.json");

        File target = new File(String.format("%s/osprey.json", System.getProperty("user.dir")));

        if (target.exists()) {
            logger.fatal("Current directory already contains file named osprey.json!");
            return -1;
        }

        try {
            FileUtils.copyInputStreamToFile(blankJsonStream, target);
            logger.info("Created blank project");
        } catch (IOException e) {
            logger.fatal(e.getMessage(), e);
            return -1;
        }
        return 0;
    }
}
