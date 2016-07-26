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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rfksystems.commander.Commander;
import com.rfksystems.commander.exception.CommandUnknownException;
import com.rfksystems.commander.exception.InputParseException;
import com.rfksystems.commander.exception.NoCommandGivenException;
import com.rfksystems.commander.exception.RuntimeArgumentException;
import org.apache.logging.log4j.Logger;

import java.net.JarURLConnection;

public class Main {
    private static final long SEC_IN_30D = 2592000;

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MainModule());
        Commander commander = injector.getInstance(Commander.class);

        Long buildTime = getBuildTime();

        if (null != buildTime && (System.currentTimeMillis() - buildTime) / 1000 > SEC_IN_30D) {
            injector.getInstance(Logger.class).warn("This installation is more than 30 days old. Please, update via https://github.com/mobilecashout/osprey");
        }

        try {
            int status = commander.execute(args);
            System.exit(status);
        } catch (NoCommandGivenException | CommandUnknownException | RuntimeArgumentException | InputParseException e) {
            e.printStackTrace();
        }

    }

    private static Long getBuildTime() {
        try {
            String mainClassfile = Main.class.getName().replace('.', '/') + ".class";
            JarURLConnection jarURLConnection = (JarURLConnection) ClassLoader.getSystemResource(mainClassfile).openConnection();
            return jarURLConnection.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
        } catch (Exception e) {
            return null;
        }
    }
}
