package com.brightsparklabs.gradle.multigit

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ConfigurationTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private File buildFile

    @Before
    public void setup() {
        // Prepare build.gradle
        buildFile = testProjectDir.newFile('build.gradle') << """\
            plugins {
                id "com.brightsparklabs.gradle.multi-git"
            }
            """.stripIndent()
    }

    /**
     * Runs a gradle build in the test project
     * @param shouldSucceed true if you expect the build to succeed, false if you expect it to fail
     * @param args The arguments to run the Gradle build with
     * @return the build result as a BuildResult object
     */
    private BuildResult build(boolean shouldSucceed, String... args) {
        def runner = GradleRunner.create()
                .withArguments(args)
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withDebug(true)
        return shouldSucceed ? runner.build() : runner.buildAndFail()
    }



    @Test
    public void basic_configuration() {
        buildFile.append("""\
            multiGitPluginConfig {
                repositoriesDir = new File('${testProjectDir.root.absolutePath}')
                repositories = [
                    'project-alpha': 'git@github.com:praqma-thi/project-alpha.git',
                    'project-bravo': ['git@github.com:praqma-thi/project-bravo.git'],
                ]
            }
            """.stripIndent())

        BuildResult result = build(true, 'gitClone', '--stacktrace')
        assert result.task(':gitClone').outcome == SUCCESS
    }

    @Test
    public void depth_configuration() {
        buildFile.append("""\
            multiGitPluginConfig {
                repositoriesDir = new File('${testProjectDir.root.absolutePath}')
                repositories = [
                    'project-alpha': 'git@github.com:praqma-thi/project-alpha.git',
                    'project-bravo': ['git@github.com:praqma-thi/project-bravo.git', 1],
                ]
            }
            """.stripIndent())

        BuildResult result = build(true, 'gitClone')
        assert result.task(':gitClone').outcome == SUCCESS

        def log = "git log --oneline".execute([], new File(testProjectDir.root, 'project-bravo')).text
        assert log.readLines().size() == 1
    }

    @Test
    public void map_configuration() {
        buildFile.append("""\
            multiGitPluginConfig {
                repositoriesDir = new File('${testProjectDir.root.absolutePath}')
                repositories = [
                    'project-alpha': [
                            url: 'git@github.com:praqma-thi/project-alpha.git'
                        ],
                    'project-bravo': [
                            url: 'git@github.com:praqma-thi/project-bravo.git',
                            options: "--depth 1 --branch develop"
                        ],
                ]
            }
            """.stripIndent())

        BuildResult result = build(true, 'gitClone')
        assert result.task(':gitClone').outcome == SUCCESS

        def log = "git log --oneline".execute([], new File(testProjectDir.root, 'project-bravo')).text
        assert log.readLines().size() == 1

        def branch = "git branch".execute([], new File(testProjectDir.root, 'project-bravo')).text
        assert branch.contains("* develop")
    }
}
