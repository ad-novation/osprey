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

package com.mobilecashout.osprey.plugin;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class RolesUtil {
    public static ImmutablePair<String, String[]> parseCommandRoles(String command) {
        String[] roles = {"all"};

        if (command.startsWith(":")) {
            String[] parts = command.split("\\s", 2);
            roles = parts[0].substring(1).split(",");
            if (parts.length == 2) {
                command = parts[1];
            } else {
                command = "";
            }
        }

        return new ImmutablePair<>(command, roles);
    }
}
