# gradle-multi-git

Gradle plugin to simplify managing multiple git repositories.

# Build

```shell
./gradlew build

# publish
./gradlew publishPlugins
```

# Usage

```groovy
// file: build.gradle

plugins {
    id 'com.brightsparklabs.gradle.multi-git'
}
```

# Configuration

Use the following configuration block to configure the plugin:

```groovy
// file: build.gradle

multiGitPluginConfig {
    repositoriesDir = new File('subprojects')
    repositories = [
        'project-alpha': 'git@github.com:brightsparklabs/project-alpha.git',
        'project-bravo': 'git@github.com:brightsparklabs/project-bravo.git',
    ]
}
```

Where:

- `repositoriesDir` is the directory to checkout the git repositories to.
- `repositories` is a map of `repository name` to `repository location`. Each
  repository will be checked out to the `repositoriesDir`.

# Tasks

The tasks added by the plugin can be found by running:

```shell
./gradlew tasks
```

# Methods

The following methods are added to the project:

- `gitExec(repoName, gitArgs)`
    - Executes a git command on the specified git repository.
    - Arguments:
        - `repoName`: Name of the git repository to execute the command on.
        - `gitArgs`: Array of arguments to append to the 'git' command.
            - E.g. `['checkout', 'master']`
- `gitExecAllRepos(gitArgs)`
    - Executes a git command on all git repositories.
    - Arguments:
        - `gitArgs`: Array of arguments to append to the 'git' command.
            - E.g. `['checkout', 'master']`

# Licenses

Refer to the `LICENSE` file for details.

