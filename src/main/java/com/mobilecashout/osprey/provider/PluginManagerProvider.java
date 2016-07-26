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

package com.mobilecashout.osprey.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mobilecashout.osprey.plugin.PluginManager;
import com.mobilecashout.osprey.plugin.plugins.*;

public class PluginManagerProvider implements Provider<PluginManager> {
    @Inject
    private LocalShellPlugin localShellPlugin;
    @Inject
    private SlackPlugin slackPlugin;
    @Inject
    private ExternalCommandListPlugin externalCommandListPlugin;
    @Inject
    private RemoteShellPlugin remoteShellPlugin;
    @Inject
    private EchoPlugin echoPlugin;
    @Inject
    private ClonePlugin clonePlugin;
    @Inject
    private PackagePlugin packagePlugin;
    @Inject
    private PushArtifactPlugin pushArtifactPlugin;
    @Inject
    private CommonCleanupPlugin commonCleanupPlugin;
    @Inject
    private ReleasePlugin releasePlugin;
    @Inject
    private RemoveOldReleasesPlugin removeOldReleasesPlugin;
    @Inject
    private RollbackPlugin rollbackPlugin;

    @Override
    public PluginManager get() {
        PluginManager pluginManager = new PluginManager();

        pluginManager.add(
                localShellPlugin,
                remoteShellPlugin,
                slackPlugin,
                externalCommandListPlugin,
                echoPlugin,
                clonePlugin,
                packagePlugin,
                pushArtifactPlugin,
                commonCleanupPlugin,
                releasePlugin,
                removeOldReleasesPlugin,
                rollbackPlugin
        );

        return pluginManager;
    }
}
