/*
 * Created by brightSPARK Labs
 * www.brightsparklabs.com
 */

package com.brightsparklabs.gradle.multigit

/**
 * Configuration object for the MultiGit plugin.
 */
class MultiGitPluginExtension {
    /** Directory to checkout the repositories to. */
    File repositoriesDir = new File('subprojects')

    /** Mapping of 'repoName : repoUrl' of all dependent git repositories. */
    Map repositories = [:]
}

