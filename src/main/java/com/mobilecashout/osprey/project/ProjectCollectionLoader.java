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

package com.mobilecashout.osprey.project;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.mobilecashout.osprey.project.config.Project;
import com.mobilecashout.osprey.project.config.ProjectCollection;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class ProjectCollectionLoader {
    @Inject
    private Gson gson;

    public ProjectCollection loadFromFile(String filename) throws FileNotFoundException, ProjectConfigurationError {
        FileReader fileReader = new FileReader(filename);
        JsonReader reader = new JsonReader(fileReader);
        Project[] projects = gson.fromJson(reader, Project[].class);
        return new ProjectCollection(projects);
    }
}
