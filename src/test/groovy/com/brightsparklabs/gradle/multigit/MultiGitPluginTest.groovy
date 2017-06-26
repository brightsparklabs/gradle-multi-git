package com.brightsparklabs.gradle.multigit

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class MultiGitPluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    private final String repo = "https://github.com/brightsparklabs/gradle-multi-git.git"

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
    public void configuration_standard() {
        buildFile.append("""\
            multiGitPluginConfig {
                repositoriesDir = new File('${testProjectDir.root.absolutePath}')
                repositories = [
                    'project-alpha': '$repo',
                    'project-bravo': '$repo',
                ]
            }
            """.stripIndent())

        BuildResult result = build(true, 'gitClone', '--stacktrace')
        assert result.task(':gitClone').outcome == SUCCESS
    }

    @Test
    public void configuration_with_depth() {
        buildFile.append("""\
            multiGitPluginConfig {
                repositoriesDir = new File('${testProjectDir.root.absolutePath}')
                repositories = [
                    'project-alpha': ['$repo'],
                    'project-bravo': ['$repo', 1],
                ]
            }
            """.stripIndent())

        BuildResult result = build(true, 'gitClone')
        assert result.task(':gitClone').outcome == SUCCESS

        def log = "git log --oneline".execute([], new File(testProjectDir.root, 'project-bravo')).text
        assert log.readLines().size() == 1
    }

    @Test
    public void configuration_with_options() {
        buildFile.append("""\
            multiGitPluginConfig {
                repositoriesDir = new File('${testProjectDir.root.absolutePath}')
                repositories = [
                    'project-alpha': [
                            url: '$repo'
                        ],
                    'project-bravo': [
                            url: '$repo',
                            options: ['--depth', '1', '--branch', 'develop']
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
