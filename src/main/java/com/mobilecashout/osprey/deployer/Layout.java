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

public class Layout {
    private String releasesDirectoryName = "releases";
    private String currentLinkName = "current";

    public String releasesDirectoryName() {
        return releasesDirectoryName;
    }

    public String currentLinkName() {
        return currentLinkName;
    }

    public String relativeReleasesPath(String deploymentRoot) {
        return deploymentRoot.concat("/").concat(releasesDirectoryName()).replace("//", "/");
    }

    public String relativeCurrentRoot(String deploymentRoot) {
        return deploymentRoot.concat("/").concat(currentLinkName()).replace("//", "/");
    }
}
