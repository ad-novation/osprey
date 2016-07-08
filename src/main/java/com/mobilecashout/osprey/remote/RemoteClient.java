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

import com.jcraft.jsch.*;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.mobilecashout.osprey.project.config.Environment;
import com.mobilecashout.osprey.deployer.DeploymentActionError;
import com.mobilecashout.osprey.deployer.DeploymentContext;
import com.mobilecashout.osprey.project.config.Target;
import org.apache.commons.exec.LogOutputStream;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.*;

public class RemoteClient {
    private final Logger logger;
    private final ArrayList<RemoteTarget> sessions = new ArrayList<>();
    private final Semaphore semaphore = new Semaphore(1);

    public RemoteClient(Environment environment, DeploymentContext context, Logger logger) throws RemoteClientException {
        RemoteClient instance = this;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                instance.tryDisconnect();
            }
        });

        this.logger = logger;

        try {
            JSch shellClient = new JSch();
            shellClient.setKnownHosts(new FileInputStream(new File(System.getenv("HOME") + "/.ssh/known_hosts")));
            ConnectorFactory connectorFactory = ConnectorFactory.getDefault();
            Connector connector = connectorFactory.createConnector();
            Properties config = new java.util.Properties();
            config.put("PreferredAuthentications", "publickey");

            if (context.project().options().containsKey("check_host_keys")) {
                String option = (boolean) context.project().options().get("check_host_keys") ? "yes" : "no";
                JSch.setConfig("StrictHostKeyChecking", option);
                if (option.equals("no")) {
                    logger.warn("WARNING: host key check is disabled!");
                }
            }

            if (connector != null) {
                IdentityRepository remoteIdentityRepository = new RemoteIdentityRepository(connector);
                shellClient.setIdentityRepository(remoteIdentityRepository);
            }
            for (Target target : environment.targets()) {
                sessions.add(new RemoteTarget(connect(shellClient, target, context, config), target, context, environment));
            }
        } catch (AgentProxyException | FileNotFoundException | JSchException e) {
            throw new RemoteClientException(e.getMessage());
        }
    }

    private Session connect(JSch shellClient, Target target, DeploymentContext context, Properties config) throws RemoteClientException {
        String username = context.substitutor().replace(target.userName());
        String host = context.substitutor().replace(target.host());

        try {
            Session session = shellClient.getSession(
                    username,
                    host,
                    target.port()
            );

            session.setConfig(config);
            session.connect();
            return session;

        } catch (JSchException e) {
            throw new RemoteClientException(String.format(e.getMessage() + " [%s@%s:%d]", username, host, target.port()));
        }
    }


    private synchronized void executeInParallel(RemoteRunnable runnable, DeploymentContext context) {

        while (!semaphore.tryAcquire()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                context.setFailed();
                throw new RuntimeException(e);
            }
        }

        final ExecutorService pool = Executors.newFixedThreadPool(sessions.size());
        final ArrayList<Future<Boolean>> futures = new ArrayList<>();

        for (RemoteTarget remoteTarget : sessions) {
            Future<Boolean> executor = pool.submit(() -> {
                runnable.run(remoteTarget, context);
                return true;
            });
            futures.add(executor);
        }

        pool.shutdown();

        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
            for (Future future : futures) {
                future.get();
            }
            Thread.sleep(100);
        } catch (InterruptedException | ExecutionException e) {
            context.setFailed();
            logger.fatal(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
    }

    public void uploadArtifact(DeploymentContext context) {
        executeInParallel(new RemoteRunnable() {
            @Override
            public void run(RemoteTarget remoteTarget, DeploymentContext context) throws DeploymentActionError {
                try {
                    Session session = remoteTarget.getSession();
                    Target target = remoteTarget.getTarget();

                    ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                    sftp.connect();

                    try {
                        sftp.cd(target.deploymentRoot());
                    } catch (SftpException e) {
                        if (2 == e.id) {
                            throw new DeploymentActionError(String.format("Failed to CD into deployment root %s, directory does not exist", target.deploymentRoot()));
                        } else {
                            throw e;
                        }
                    }

                    createIfNotExists(sftp, remoteTarget.getRelativeReleasesRoot());

                    createIfNotExists(sftp, remoteTarget.getRelativeCurrentReleaseRoot());

                    sftp.cd(remoteTarget.getRelativeCurrentReleaseRoot());

                    String artifactName = String.format("%d.tar.gz", context.releaseId());

                    sftp.put(new FileInputStream(context.buildArtifact()), artifactName);

                    sftp.disconnect();

                    while (!sftp.isClosed()) {
                        Thread.sleep(10);
                    }

                    ChannelExec exec = (ChannelExec) session.openChannel("exec");
                    exec.setCommand(String.format("cd %1$s;tar xf %2$s;rm %2$s", remoteTarget.getRelativeCurrentReleaseRoot(), artifactName));
                    exec.connect();
                    exec.start();

                    logger.info("Uploaded and extracted artifact to {}", target.host());

                    while (!exec.isClosed()) {
                        Thread.sleep(10);
                    }

                    exec.disconnect();

                } catch (JSchException | SftpException | FileNotFoundException | InterruptedException e) {
                    throw new DeploymentActionError(e);
                }
            }

            private void createIfNotExists(ChannelSftp sftp, String directory) throws DeploymentActionError {
                try {
                    SftpATTRS releasesAttributes = sftp.stat(directory);

                    if (!releasesAttributes.isDir()) {
                        throw new DeploymentActionError(String.format("Releases root %s is not a directory", directory));
                    }
                } catch (SftpException e) {
                    try {
                        sftp.mkdir(directory);
                    } catch (SftpException e1) {
                        throw new DeploymentActionError(String.format("Unable to create directory %s", directory));
                    }
                }
            }
        }, context);
    }

    public void tryDisconnect() {
        sessions.stream().filter(remoteTarget -> remoteTarget.getSession().isConnected()).forEach(remoteTarget -> {
            remoteTarget.getSession().disconnect();
        });
    }

    public void symlinkCurrentRelease(DeploymentContext context) {
        executeInParallel((remoteTarget, innerContext) -> {
            try {
                Target target = remoteTarget.getTarget();

                final ChannelSftp sftp = (ChannelSftp) remoteTarget.getSession().openChannel("sftp");
                sftp.connect();
                try {
                    sftp.cd(target.deploymentRoot());
                } catch (SftpException e) {
                    if (2 == e.id) {
                        throw new DeploymentActionError(String.format("Failed to CD into deployment root %s, directory does not exist", target.deploymentRoot()));
                    } else {
                        throw e;
                    }
                }

                try {
                    sftp.rm(remoteTarget.getCurrentLink());
                } catch (SftpException e) {
                    // @TODO nooop?
                }

                sftp.symlink(remoteTarget.getRelativeCurrentReleaseRoot(), remoteTarget.getCurrentLink());

                logger.info("Symlink to current release {} on {} created", innerContext.releaseId(), target.host());

                sftp.disconnect();

                while (!sftp.isClosed()) {
                    Thread.sleep(10);
                }
            } catch (JSchException | SftpException | InterruptedException e) {
                throw new DeploymentActionError(e);
            }
        }, context);
    }

    public synchronized void execute(String command, DeploymentContext context) {
        executeInParallel((remoteTarget, innerContext) -> {
            try {
                final ChannelExec exec = (ChannelExec) remoteTarget.getSession().openChannel("exec");
                String commandSubstituted = remoteTarget.getSubstitutor().replace(String.format("cd %s;%s", remoteTarget.getRelativeCurrentReleaseRoot(), command));
                exec.setErrStream(new LogOutputStream() {
                    @Override
                    protected void processLine(String s, int i) {
                        logger.error(s);
                    }
                });
                exec.setOutputStream(new LogOutputStream() {
                    @Override
                    protected void processLine(String s, int i) {
                        logger.info(s);
                    }
                });
                exec.setCommand(commandSubstituted);
                exec.connect();
                exec.run();
                while (!exec.isClosed()) {
                    Thread.sleep(10);
                }
                exec.disconnect();
                if (0 != exec.getExitStatus()) {
                    throw new DeploymentActionError(String.format("Failed to execute %s, exit code %d", commandSubstituted, exec.getExitStatus()));
                }

                logger.info("Executed {} on {}", commandSubstituted, remoteTarget.getTarget().host());

            } catch (JSchException | InterruptedException e) {
                throw new DeploymentActionError(e);
            }
        }, context);
    }
}
