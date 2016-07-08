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

package com.mobilecashout.osprey.project.config;

import com.mobilecashout.osprey.project.ProjectConfigurationError;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProjectCollection {
    private HashMap<String, Project> projects = new HashMap<>();

    public ProjectCollection(Project[] projectsArray) throws ProjectConfigurationError {
        for (Project project : projectsArray) {
            String name = project.getName();
            if (projects.containsKey(name)) {
                throw new ProjectConfigurationError(String.format("Duplicate project name %s", name));
            }
            projects.put(project.getName(), project);
        }
    }

    public int size() {
        return projects.size();
    }

    public boolean isEmpty() {
        return projects.isEmpty();
    }

    public Project get(String key) {
        return projects.get(key);
    }

    public boolean contains(String key) {
        return projects.containsKey(key);
    }

    public Set<Map.Entry<String, Project>> entrySet() {
        return projects.entrySet();
    }
}
