/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertTrue

/**
 * A simple functional test for the 'in.praj.kawa' plugin.
 */
class KawaPluginFunctionalTest {
    @Rule @JvmField
    val tempDir = TemporaryFolder()
    private lateinit var projectDir: File
    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @Before
    fun setup() {
        projectDir = tempDir.newFolder()
        settingsFile = projectDir.resolve("settings.gradle.kts")
        buildFile = projectDir.resolve("build.gradle.kts")
    }

    @Test
    fun `test downloadToolsKawa task`() {
        settingsFile.writeText("""
            rootProject.name = "test-kawa-tools"
        """.trimIndent())

        buildFile.writeText("""
            plugins {
              id("in.praj.kawa")
            }
            
            kawa {
              version.set("3.2.1")
            }
        """.trimIndent())

        val result = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("downloadToolsKawa")
                .withProjectDir(projectDir)
                .build()
        assertTrue(result.output.contains("Downloading version 3.2.1"))
    }
}
