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

package com.mobilecashout.osprey.plugin.plugins;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.mobilecashout.osprey.deployer.DeploymentAction;
import com.mobilecashout.osprey.deployer.DeploymentActionError;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.deployer.DeploymentPlan;
import com.mobilecashout.osprey.plugin.PluginInterface;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class SlackPlugin implements PluginInterface {
    @Inject
    private Logger logger;

    @Override
    public String getName() {
        return "slack";
    }

    @Override
    public String[] getEnvironments() {
        return new String[]{
                PluginInterface.LOCAL
        };
    }

    @Override
    public DeploymentAction[] actionFromCommand(String command, DeploymentPlan deploymentPlan, DeploymentContext deploymentContext) {
        return new DeploymentAction[]{
                new SlackAction(deploymentContext.substitutor().replace(command), logger)
        };
    }

    private static class SlackAction implements DeploymentAction {
        private final String command;
        private final Logger logger;

        public SlackAction(String command, Logger logger) {
            this.command = command;
            this.logger = logger;
        }

        @Override
        public String getDescription() {
            return "Send Slack message: " + command;
        }

        @Override
        public void execute(DeploymentContext context) throws DeploymentActionError {
            String slackUrl = context.substitutor().replace("{slack}");
            if (slackUrl.length() == 0) {
                logger.warn("Slack URL is not set. Please, define slack URL.");
                return;
            }

            try {
                Gson gson = new Gson();
                Message message = new Message(command);
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(slackUrl);
                StringEntity postingString = new StringEntity(gson.toJson(message));
                post.setEntity(postingString);
                post.setHeader("Content-type", "application/json");
                HttpResponse response = httpClient.execute(post);
            } catch (IOException e) {
                logger.warn(e);
            }

        }
    }

    private static class Message {
        String username = "Osprey";
        String icon_emoji = ":osprey:";
        String text = "";

        public Message(String text) {
            this.text = text;
        }
    }
}
