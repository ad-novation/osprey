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

package com.mobilecashout.osprey.remote;

import com.jcraft.jsch.Session;
import com.mobilecashout.osprey.project.config.Environment;
import com.mobilecashout.osprey.project.config.Target;
import com.mobilecashout.osprey.deployer.DeploymentContext;

import java.util.HashMap;

public class RemoteTarget {
    private final String relativeReleasesRoot;
    private final String relativeCurrentReleaseRoot;
    private final String artifactName;
    private final WrappedSubstitutor substitutor;
    private final String currentLink;
    private Session session;
    private Target target;

    public RemoteTarget(Session session, Target target, DeploymentContext context, Environment environment) {
        this.session = session;
        this.target = target;

        this.relativeReleasesRoot = context.layout().relativeReleasesPath(target.deploymentRoot());
        this.relativeCurrentReleaseRoot = relativeReleasesRoot.concat("/").concat(String.valueOf(context.releaseId())).replace("//", "/");
        this.artifactName = String.format("%d.tar.gz", context.releaseId());
        this.currentLink = context.layout().relativeCurrentRoot(target.deploymentRoot());

        HashMap<String, String> variables = new HashMap<>();
        variables.put("current_release", relativeCurrentReleaseRoot);
        variables.put("releases_root", relativeReleasesRoot);
        variables.put("artifact_name", artifactName);
        variables.put("current_release_link", currentLink);

        this.substitutor = new WrappedSubstitutor(variables, context.substitutor());
    }

    public Session getSession() {
        return session;
    }

    public Target getTarget() {
        return target;
    }

    public String getRelativeReleasesRoot() {
        return relativeReleasesRoot;
    }

    public String getRelativeCurrentReleaseRoot() {
        return relativeCurrentReleaseRoot;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public String getCurrentLink() {
        return currentLink;
    }

    public WrappedSubstitutor getSubstitutor() {
        return substitutor;
    }
}
