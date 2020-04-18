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
        settingsFile = projectDir.resolve("settings.gradle")
        buildFile = projectDir.resolve("build.gradle")
    }

    @Test
    fun `test downloadToolsKawa task`() {
        settingsFile.writeText("""
            rootProject.name = "test-kawa-tools"
        """.trimIndent())

        buildFile.writeText("""
            plugins {
              id "in.praj.kawa"
            }
            
            kawa {
              version = "1.10"
            }
        """.trimIndent())

        val result = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("downloadToolsKawa")
                .withProjectDir(projectDir)
                .build()
        assert(result.output.contains("Downloaded Kawa 1.10"))
    }
}
