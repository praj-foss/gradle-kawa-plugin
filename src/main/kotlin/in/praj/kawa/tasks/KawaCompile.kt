/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile

/**
 * Task to compile Kawa source files.
 */
abstract class KawaCompile : AbstractCompile() {
    @get:Input
    abstract val language: Property<String>

    @get:Input
    abstract val args: Property<String>

    @TaskAction
    override fun compile() {
        val compileClasspath = classpath.joinToString(separator = ":")

        // Define Kawac task
        withAnt("taskdef", mapOf(
                "name"      to "kawac",
                "classname" to "gnu.kawa.ant.Kawac",
                "classpath" to compileClasspath
        ))

        // Compile Kawa sources
        withAnt("kawac", mapOf(
                "language" to language.get(),
                "classpath" to compileClasspath,
                "destdir" to destinationDir,
                "target" to targetCompatibility,
                "warnaserror" to true
        )) {
            withAnt("arg", mapOf("line" to args.get()))
            source.addToAntBuilder(ant, "fileset", FileCollection.AntType.FileSet)
        }
        logger.lifecycle("Compiled Kawa sources")
    }
}