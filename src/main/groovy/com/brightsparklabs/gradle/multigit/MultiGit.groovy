/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.gradle.multigit

import java.util.logging.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Simplifies checking out and managing multiple git repositories.
 */
class MultiGitPlugin implements Plugin<Project> {

    Logger logger = Logger.getLogger("com.brightsparklabs.gradle.multigit.MultiGitPlugin")

    void apply(Project project) {

        // ---------------------------------------------------------------------
        // CONFIGURATION
        // ---------------------------------------------------------------------

        // Create plugin configuration object.
        def config = project.extensions.create('multiGitPluginConfig', MultiGitPluginExtension)

        // ---------------------------------------------------------------------
        // TASKS
        // ---------------------------------------------------------------------

        project.task('gitClone') {
            group = "brightSPARK Labs - Git"
            description = "Clones all git repositories which constitute this project."

            doLast {
                config.repositories.each { name, uri ->
                    def repoDir = new File(config.repositoriesDir, name)
                    if (repoDir.exists()) {
                        return
                    }
                    project.gitExec('.', ['clone', uri, repoDir.absolutePath])
                }
            }
        }

        project.task('gitPull') {
            group = "brightSPARK Labs - Git"
            description = "Runs a 'git pull' on all repositories which constitute this project."
            dependsOn project.gitClone

            doLast {
                project.gitExecAllRepos(['pull'])
            }
        }

        project.task('gitCheckoutMaster') {
            group = "brightSPARK Labs - Git"
            description = "Checks out the master branch on all repositories which constitute this project."
            dependsOn project.gitClone

            doLast {
                project.gitExecAllRepos(['checkout', 'master'])
            }
        }

        project.task('gitCheckoutDevelop') {
            group = "brightSPARK Labs - Git"
            description = "Checks out the develop branch on all repositories which constitute this project."
            dependsOn project.gitClone

            doLast {
                project.gitExecAllRepos(['checkout', 'develop'])
            }
        }

        project.task('gitInfo') {
            group = "brightSPARK Labs - Git"
            description = "Prints git information on all repositories which constitute this project."
            dependsOn project.gitClone

            doLast {
                def rows = []
                config.repositories.each { name, uri ->
                    def output = [name]
                    output << project.gitExec(name, ['describe', '--dirty'])
                    output << project.gitExec(name, ['rev-parse', '--abbrev-ref', 'HEAD'])
                    rows << output
                }

                println "Repository           | Version                        | Branch"
                println "==================== | ============================== | ================"
                rows.each { row ->
                    def repo = row[0]
                    def version = row[1].toString().trim()
                    def branch = row[2].toString().trim()
                    def repoInfo = String.format('%-20s | %-30s | %s', repo, version, branch)
                    println repoInfo
                }
            }
        }

        // ---------------------------------------------------------------------
        // METHODS
        // ---------------------------------------------------------------------

        /**
         * Executes a git command on all git repositories.
         *
         * @param gitArgs
         *          Array of arguments to append to the 'git' command.
         *          E.g. ['checkout', 'master']
         *
         * @return the ouptut from the stdout stream
         */
        project.ext.gitExecAllRepos = { gitArgs ->
            config.repositories.each { name, uri ->
                return project.gitExec(name, gitArgs)
            }
        }

        /**
         * Executes a git command on the specified git repository.
         *
         * @param repoName
         *          Name of the git repository to execute the command on.
         *
         * @param gitArgs
         *          Array of arguments to append to the 'git' command.
         *          E.g. ['checkout', 'master']
         *
         * @return the ouptut from the stdout stream
         */
        project.ext.gitExec = { repoName, gitArgs ->
            def gitCommand = ['git'] + gitArgs
            def stdout = new ByteArrayOutputStream()
            project.exec {
                workingDir new File(config.repositoriesDir, repoName)
                commandLine gitCommand
                standardOutput = stdout
            }
            return stdout.toString().trim()
        }
    }
}

