#Osprey

Simple application deployment tool written in Java with Capistrano compatible layout.

![Osprey](./deployment/osprey.medium.png "Osprey")

[![Build Status](https://travis-ci.org/mobilecashout/osprey.svg?branch=master)](https://travis-ci.org/mobilecashout/osprey)

**Disclaimer:** although stable and used in production for handful of projects,
this software is still very rough around the edges. Things can break. You are encouraged
to open an issue if they do.

## Usage

Available commands:

```
help: Display command usage information
init: Create a blank osprey.json file in current working directory
deploy: deploy given project to indicated environment
        <project>           : required, name of the project to deploy.
        <environment>       : required, name of environment to deploy to.
        [--config=file.json]: optional, configuration file path. Defaults to osprey.json.
        [--verbose]         : optional, display debug output. Note, that deployment log will
                              always record all log output anyway. Defaults to false.
        [--assume-yes]      : optional, don't prompt for any confirmations. Defaults to false.
        [--debug]           : optional, pause after before execution step
```

### Download

For Linux and Mac OS: [download binary](./dist/osprey)

### Requirements

Machine executing the tool must have Java 1.8 or greater installed. Binary releases are compatible with both Mac and Linux.

If you happen to be using Windows, I wish you good luck.

Machine must have SSH agent installed and running. The tool will proxy all authentication requests via ssh-agent,
and no other means of authentication are supported at this time.

### Installation

Just move the downloaded binary to `/usr/local/bin/osprey`. That should be it.

### Deployment configuration

Osprey deployment configuration is stored in JSON files, each configuration file can contain multiple projects.

It is possible to maintain single configuration file for many applications, if you choose to.

## Basic concepts

### Deployment flow

Each deploy is generally done in 4 steps:

1. Clone remote sources.
2. Execute local build.
3. Package and upload artifact to remote targets.
4. Expand artifact, run remote commands and symlink current release.

### Deployment log

Each execution of the tool will generate a file called `deployment.log` in current working directory.
It will contain all output from Osprey generated during output. It will be appended with deployment info
between all deployments.

### Variables

You can define a number of variables in project configuration inside `define` block. You can then
use `{variable}` format placeholders across the project's configuration, where they will be re replace with their actual
values defined here, in environment and deployment targets.

### Environment variables

Environment variables are automatically imported and interpolated on application startup. You can access them
using format `{env_<name>}` where name is a lowercased version of environment variable name.

### Plugins

Plugins are feature shortcuts the deployment lifecycle, or deployment plan is built around. Plugins allow you to perform basic
tasks meant to automate deployment process and interface with deployment tool.

#### Using a plugin

Plugins are invoked from within build by referencing them using their name. For example, `@shell echo Hello World` would execute
a `@shell` plugin with argument `echo Hello World`.

Plugins can accept arbitrary arguments to configure their behavior.

#### Plugin scopes

Each plugin can have one of 3 possible scopes - local, remote or both. Effectively, it
means that plugin will only be available on local stage, on remote stage, or both.

#### Available plugins

##### `@clone <repo> <branch>`

*Stages: local*

Execute clone inside local build directory. Currently, only GIT is supported. This plugin will execute a shallow
clone of the repository, meaning, it will only include 1 last commit from the remote branch you indicated.

*Note:* it is not mandatory to use your plugin. You can as well roll your own command using `@shell`.

##### `@cleanup`

*Stages: local*

Remove common artefacts usually not required in production, like .git/ .svn/ .hg/ .gitignore.

##### `@echo <value>`

*Stages: local, remote*

Output the value in deployment log and on screen.

##### `@from <value>`

*Stages: local*

Not implemented. Reserved for loading external task lists.

##### `@shell <value>`

*Stages: local, remote*

Run an arbitrary shell command in any given environment.

##### `@package`

*Stages: local*

Build artifact package (.tar.gz of working directory).

##### `@pushArtifact`

*Stages: local*

Upload build artifact to remotes for given target.

##### `@release`

*Stages: remote*

Publish the current release. Effectively it means, make a symlink from `/current/` to
`/releases/<current release>`.

##### `@removeAllExcept <number>`

*Stages: remote*

Clean up old releases and maintain N last releases.

##### `@rollback`

*Stages: remote*

Rollback currently removes the current release directory on the server. Further improvements
for this plugin is @TODO.

##### `@slack <message>`

*Stages: local*

Send a massage to Slack. Requires that `slack` variable is defined in variables.

### Build

In context of Osprey, the complete cycle of building and deploying application is called a build.

Each application can have one build sequence composed of multiple build steps within different categories.

`build.success`: commands executed when all stages and all tasks succeeded.

`build.failure`: commands to execute when deployment failed.

`build.local.prepare`: you should execute all build commands here, normally considered build preparation.

`build.local.success`: commands to execute when local prepare succeeded.

`build.local.failure`: commands to execute when local prepare failed.

`build.remote.prepare`: execute remote build commands, for example, creating symlinks etc. This will be executed in servers.

`build.remote.success`: commands to execute when remote prepare succeeded.

`build.remote.failure`: commands to execute when remote prepare failed.

```json
{
    "build": {
        "success": [
            "@shell echo All done"
        ],
        "failure": [
            "@shell echo All failed"
        ],
        "local": {
            "prepare": [
                "@clone {repository} {branch}",
                "@shell echo {release} > release.txt",
                "@cleanup",
                "@package"
            ],
            "success": [
                "@shell echo Local done"
            ],
            "failure": [
                "@shell echo Local failed"
            ]
        },
        "remote": {
            "prepare": [
                "@pushArtifact",
                "@removeAllExcept 3"
            ],
            "success": [
                "@release",
                "@echo Remote done"
            ],
            "failure": [
                "@rollback",
                "@echo Remote failed"
            ]
        }
    }
}
```

### Config file example

```json
[
    {
        "name": "test",
        "options": {
            "check_host_keys": true
        },
        "define": {
            "repository": "git@test.org:repo.git",
            "slack": "https://hooks.slack.com/services/SOME_SLACK_ID"
        },
        "environments": {
            "staging": {
                "define": {
                    "branch": "staging"
                },
                "targets": [
                    {
                        "user": "{user}",
                        "host": "stage.example.com",
                        "port": 22,
                        "root": "/var/www/test.com/"
                    }
                ]
            },
            "production": {
                "define": {
                    "branch": "master"
                },
                "targets": [
                    {
                        "user": "{user}",
                        "host": "example.com",
                        "port": 22,
                        "root": "/var/www/test.com/"
                    }
                ]
            }
        },
        "build": {
            "success": [
                "@slack :white_check_mark: *{user}@{hostname}* deployed *{project}* on *{environment}* from `{repository}` on `{branch}`, release *{release}*"
            ],
            "failure": [
                "@slack :x: *{user}@{hostname}* failed to deploy *{project}* on *{environment}* from `{repository}` on `{branch}`, release *{release}*"
            ],
            "local": {
                "prepare": [
                    "@clone {repository} {branch}",
                    "@cleanup",
                    "@package"
                ],
                "success": [],
                "failure": []
            },
            "remote": {
                "prepare": [
                    "@pushArtifact",
                    "@removeAllExcept 3"
                ],
                "success": [
                    "@release"
                ],
                "failure": [
                    "@rollback"
                ]
            }
        }
    }
]

```

### Building Osprey binary

Osprey binary can be built using `mvn package` and then executing `package.sh PATH_TO_JAR` to create binary.

## License

Apache 2.0 License.