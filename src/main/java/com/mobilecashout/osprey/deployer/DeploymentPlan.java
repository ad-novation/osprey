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

import com.mobilecashout.osprey.util.Prompt;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class DeploymentPlan {
    private ArrayList<DeploymentAction> localPrepare = new ArrayList<>();
    private ArrayList<DeploymentAction> localSuccess = new ArrayList<>();
    private ArrayList<DeploymentAction> localFailure = new ArrayList<>();

    private ArrayList<DeploymentAction> remotePrepare = new ArrayList<>();
    private ArrayList<DeploymentAction> remoteSuccess = new ArrayList<>();
    private ArrayList<DeploymentAction> remoteFailure = new ArrayList<>();

    private ArrayList<DeploymentAction> globalSuccess = new ArrayList<>();
    private ArrayList<DeploymentAction> globalFailure = new ArrayList<>();


    public void describe(Logger logger) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        describeItem("Local preparation: ", localPrepare, printWriter);
        describeItem("On local success: ", localSuccess, printWriter);
        describeItem("On local failure: ", localFailure, printWriter);

        describeItem("Remote preparation: ", remotePrepare, printWriter);

        describeItem("On remote success: ", remoteSuccess, printWriter);
        describeItem("On remote failure: ", remoteFailure, printWriter);

        describeItem("On global success: ", globalSuccess, printWriter);
        describeItem("On global failure: ", globalFailure, printWriter);

        logger.info("Deployment plan:\n" + writer.toString().trim());
    }

    private void describeItem(String title, ArrayList<DeploymentAction> listOfActions, PrintWriter printWriter) {
        printWriter.println("● " + title);

        if (0 == listOfActions.size()) {
            printWriter.println("\t⚠ WARNING: empty execution step. This might be significant!");
            return;
        }

        for (DeploymentAction action : listOfActions) {
            printWriter.println("\t○ " + action.getDescription());
        }
    }

    public void executeInOrder(DeploymentContext context, Logger logger) {

        logger.info("Executing project prepare");
        execute(localPrepare, context, logger);

        if (context.isFailed()) {
            logger.fatal("Project prepare failed");
            execute(localFailure, context, logger, true);
            execute(globalFailure, context, logger, true);
            return;
        } else {
            execute(localSuccess, context, logger);
        }

        logger.info("Executing remote prepare");
        execute(remotePrepare, context, logger);

        if (context.isFailed()) {
            logger.fatal("Remote prepare failed");
            execute(remoteFailure, context, logger, true);
            execute(globalFailure, context, logger, true);
            return;
        }

        execute(remoteSuccess, context, logger);
        execute(globalSuccess, context, logger);
    }

    private void execute(ArrayList<DeploymentAction> executionList, DeploymentContext context, Logger logger) {
        execute(executionList, context, logger, false);
    }

    private void execute(ArrayList<DeploymentAction> executionList, DeploymentContext context, Logger logger, boolean forceContinue) {
        for (DeploymentAction action : executionList) {
            logger.info(action.getDescription());
            if (context.isDebug() && !context.isNonInteractive()) {
                Prompt.pause("Press Enter to continue");
            }
            try {
                action.execute(context);
            } catch (DeploymentActionError deploymentActionError) {
                context.setFailed();
                if (context.isVerbose()) {
                    logger.fatal(deploymentActionError);
                } else {
                    logger.fatal(deploymentActionError.getMessage());
                }
                return;
            }
            if (context.isFailed()) {
                return;
            }
        }
    }

    public ArrayList<DeploymentAction> getLocalPrepare() {
        return localPrepare;
    }

    public ArrayList<DeploymentAction> getLocalSuccess() {
        return localSuccess;
    }

    public ArrayList<DeploymentAction> getLocalFailure() {
        return localFailure;
    }

    public ArrayList<DeploymentAction> getRemotePrepare() {
        return remotePrepare;
    }

    public ArrayList<DeploymentAction> getRemoteSuccess() {
        return remoteSuccess;
    }

    public ArrayList<DeploymentAction> getRemoteFailure() {
        return remoteFailure;
    }

    public ArrayList<DeploymentAction> getGlobalSuccess() {
        return globalSuccess;
    }

    public ArrayList<DeploymentAction> getGlobalFailure() {
        return globalFailure;
    }
}
