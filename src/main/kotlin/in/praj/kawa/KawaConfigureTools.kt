/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import groovy.lang.Closure
import org.apache.tools.ant.PropertyHelper
import org.gradle.api.AntBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Task to configure and build the downloaded Kawa tools.
 */
open class KawaConfigureTools @Inject constructor(
        private val extension: KawaExtension
) : DefaultTask() {
    @InputFile
    val buildXml: Provider<File> = extension.kawaBuildDir
            .map { it.dir("kawa-${extension.version.get()}").file("build.xml").asFile }

    @TaskAction
    fun perform() {
        ant.lifecycleLogLevel = AntBuilder.AntMessagePriority.VERBOSE
        val buildXml = buildXml.get()

        val baseDir = buildXml.parentFile
        val toolsDir = baseDir.resolve("tools")

        buildTools(baseDir, toolsDir)
        defineToolTasks(toolsDir)
        preprocessClasses(baseDir)
        generateSimpleVectorFiles(baseDir, toolsDir)
    }

    private fun buildTools(srcDir: File, destDir: File) {
        withAnt("mkdir", mapOf("dir" to destDir))
        withAnt("javac", mapOf(
                "srcdir"            to srcDir,
                "destdir"           to destDir,
                "includes"          to listOf(
                        "gnu/kawa/util/PreProcess.java",
                        "gnu/kawa/ant/*.java")
                        .joinToString(),
                "classpath"         to ant.properties["ant.core.lib"]!!.toString(),
                "includeantruntime" to true,
                "optimize"          to true,
                "deprecation"       to false,
                "fork"              to true,
                "debug"             to true
        ))
        println("\n===> Finished compiling Kawa tools")
    }

    private fun defineToolTasks(toolsDir: File) {
        withAnt("taskdef", mapOf(
                "name"      to "kawac",
                "classname" to "gnu.kawa.ant.Kawac",
                "classpath" to toolsDir
        ))
        withAnt("taskdef", mapOf(
                "name"      to "xcopy",
                "classname" to "gnu.kawa.ant.XCopy",
                "classpath" to toolsDir
        ))
    }

    private fun preprocessClasses(srcDir: File) {
        withAnt("mkdir", mapOf("dir" to srcDir))
        withAnt("xcopy", mapOf(
                "todir" to srcDir,
                "overwrite" to false
        )) {
            withAnt("fileset", mapOf(
                    "dir" to srcDir.resolve("kawa"),
                    "includes" to "*.java.in"))
            withAnt("mapper", mapOf(
                    "type" to "glob",
                    "from" to "*Version.java.in",
                    "to" to "*Version.java"))
            withAnt("filterset", mapOf(
                    "begintoken" to "-*-",
                    "endtoken" to "-*-")) {
                withAnt("filter", mapOf(
                        "token" to "Java",
                        "value" to "Automatically generated file - DO NOT EDIT!.  -*- buffer-read-only: t -*-"))
            }
            withAnt("filterset", mapOf(
                    "begintoken" to "\"",
                    "endtoken" to "\"")) {
                withAnt("filter", mapOf(
                        "token" to "VERSION",
                        "value" to "\"${extension.version.get()}\""))
            }
        }
        println("\n===> Finished preprocessing, with XCopy")
    }

    private fun generateSimpleVectorFiles(srcDir: File, toolsDir: File) {
        fun makePrimeVectorSource(tag: String, out: String) =
                withAnt("java", mapOf("classname" to "gnu.kawa.util.PreProcess")) {
                    withAnt("classpath", mapOf("location" to toolsDir))
                    listOf(
                            "%$tag",
                            "%UniformVector",
                            "-o",
                            "gnu/lists/${out}Vector.java",
                            "gnu/lists/PrimVector.template"
                    ).map { withAnt("arg", mapOf("value" to it)) }
                }

        val vectorTypes = arrayListOf(
                "F", "Bit", "Byte", "Short", "Int", "Long", "F32", "F64",
                "S8", "S16", "S32", "S64", "U8", "U16", "U32", "U64")

        // Preprocess files unless they're up to date
        val property = "simplevector-files-uptodate"
        withAnt("uptodate", mapOf("property" to property)) {
            withAnt("srcfiles", mapOf("file" to srcDir.resolve(File("gnu/lists/PrimVector.template"))))
            withAnt("srcfiles", mapOf("file" to toolsDir.resolve(File("gnu/kawa/util/PreProcess.class"))))
            withAnt("compositemapper") {
                vectorTypes.map { srcDir.resolve(File("gnu/lists/${it}Vector.java")) }
                        .map { withAnt("mergemapper", mapOf("to" to it)) }
            }
        }
                .takeUnless { PropertyHelper.toBoolean(property.fromAnt()) }
                ?.also {
                    makePrimeVectorSource("OBJ", vectorTypes[0])
                    vectorTypes.drop(1)
                            .map { makePrimeVectorSource(it.toUpperCase(Locale.ENGLISH), it) }
                }
        println("\n===> Finished generating Vector files")
    }

    private fun withAnt(
            name: String,
            props: Map<String, Any>? = null,
            closure: (() -> Any)? = null
    ) = if (props == null && closure == null)
            ant.invokeMethod(name)
        else if (closure == null)
            ant.invokeMethod(name, props)
        else {
            object : Closure<Any?>(null) {
                override fun call(vararg args: Any?): Any? {
                    return closure.invoke()
                }
            }.let { ant.invokeMethod(name,
                    if (props == null) it
                    else arrayOf(props, it)
            ) }
        }

    private fun String.fromAnt() = PropertyHelper.getProperty(ant.antProject, this)
}
