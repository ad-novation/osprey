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

package com.mobilecashout.osprey.deployer;

import com.google.common.io.Files;
import com.mobilecashout.osprey.project.config.Environment;
import com.mobilecashout.osprey.project.config.Project;
import com.mobilecashout.osprey.remote.RemoteClient;
import com.mobilecashout.osprey.util.Substitutor;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DeploymentContext {
    private final Project project;
    private final Environment environment;
    private final boolean verbose;
    private final boolean nonInteractive;
    private final File buildDirectory;
    private final File buildArtifact;
    private final boolean debug;
    private final Substitutor substitutor;
    private final Layout layout;
    private boolean isFailed = false;
    private long release = System.currentTimeMillis();
    private RemoteClient remoteClient;

    public DeploymentContext(Project project, Environment environment, String environmentName, boolean verbose, boolean assumeYes, boolean debug) {
        DeploymentContext instance = this;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (instance.buildDirectory().exists()) {
                    instance.buildDirectory().delete();
                }
                if (instance.buildArtifact().exists()) {
                    instance.buildArtifact().delete();
                }
            }
        });

        File tmpDir = Files.createTempDir();

        String localBuildNomenclature = String.format(
                "%s/osprey_deploy_%d",
                tmpDir.toString(),
                release
        );

        this.project = project;
        this.environment = environment;
        this.verbose = verbose;
        this.nonInteractive = assumeYes;
        this.debug = debug;

        this.buildDirectory = new File(localBuildNomenclature + "/");
        this.buildArtifact = new File(localBuildNomenclature + ".tar.gz");
        this.substitutor = new Substitutor(this.project.variables());

        this.layout = new Layout();

        substitutor.add("release", release);
        substitutor.add("tmp", buildDirectory.toString());
        substitutor.add("artifact", buildArtifact.toString());
        substitutor.add("project", project.getName());
        substitutor.add("debug", debug);
        substitutor.add("verbose", verbose);
        substitutor.add("non_interactive", nonInteractive);
        substitutor.add("environment", environmentName);

        substitutor.addAll(project.variables());
        substitutor.addAll(environment.variables());

        if (!substitutor.contains("user")) {
            substitutor.add("user", substitutor.get("env_user"));
        }
        if (!substitutor.contains("hostname")) {
            try {
                substitutor.add("hostname", InetAddress.getLocalHost().getCanonicalHostName());
            } catch (UnknownHostException e) {
                substitutor.add("hostname", "???");
            }
        }
    }

    public Project project() {
        return project;
    }

    public Environment environment() {
        return environment;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isNonInteractive() {
        return nonInteractive;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setFailed() {
        isFailed = true;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public long releaseId() {
        return release;
    }

    public File buildDirectory() {
        return buildDirectory;
    }

    public File buildArtifact() {
        return buildArtifact;
    }

    public Substitutor substitutor() {
        return substitutor;
    }

    public void remoteClient(RemoteClient remoteClient) {
        this.remoteClient = remoteClient;
    }

    public RemoteClient remoteClient() {
        return remoteClient;
    }

    public Layout layout() {
        return layout;
    }
}
