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
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale
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

    @OutputDirectory
    val kawaDist: Provider<File> = buildXml.map { it.resolveSibling("dist") }

    @TaskAction
    fun perform() {
        ant.lifecycleLogLevel = AntBuilder.AntMessagePriority.VERBOSE
        val buildXml = buildXml.get()

        val baseDir = buildXml.parentFile
        val toolsDir = baseDir.resolve("tools")
        val kawaBuildDir = baseDir.resolve("classes")
        val kawaDistDir = kawaDist.get()
        ant.antProject.baseDir = baseDir

        buildTools(baseDir, toolsDir)
        defineToolTasks(toolsDir)
        preprocessClasses(baseDir)
        generateSimpleVectorFiles(baseDir, toolsDir)
        buildCore(baseDir, kawaBuildDir)
        buildLib(baseDir, kawaBuildDir)
        buildSlib(baseDir, kawaBuildDir)
        generateJar(baseDir, kawaBuildDir, kawaDistDir)
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
                "todir" to srcDir.resolve("kawa"),
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
                            "%${tag}",
                            "%UniformVector",
                            "-o",
                            srcDir.resolve("gnu/lists/${out}Vector.java"),
                            srcDir.resolve("gnu/lists/PrimVector.template")
                    ).map { withAnt("arg", mapOf("value" to it)) }
                }

        val vectorTypes = arrayListOf(
                "F", "Bit", "Byte", "Short", "Int", "Long", "F32", "F64",
                "S8", "S16", "S32", "S64", "U8", "U16", "U32", "U64")

        // Preprocess files unless they're up to date
        val upToDate = "simplevector-files-uptodate"
        withAnt("uptodate", mapOf("property" to upToDate)) {
            withAnt("srcfiles", mapOf(
                    "file" to srcDir.resolve("gnu/lists/PrimVector.template")))
            withAnt("srcfiles", mapOf(
                    "file" to toolsDir.resolve("gnu/kawa/util/PreProcess.class")))
            withAnt("compositemapper") {
                vectorTypes.map { srcDir.resolve("gnu/lists/${it}Vector.java") }
                        .map { withAnt("mergemapper", mapOf("to" to it)) }
            }
        }

        if (PropertyHelper.getPropertyHelper(ant.antProject)
                        .testUnlessCondition(upToDate)) {
            makePrimeVectorSource("OBJECT", vectorTypes[0])
            vectorTypes.drop(1)
                    .map { makePrimeVectorSource(it.toUpperCase(Locale.ENGLISH), it) }
        }
        println("\n===> Finished generating Vector files")
    }

    private fun buildCore(srcDir: File, buildDir: File) {
        withAnt("mkdir", mapOf("dir" to buildDir))
        withAnt("javac", mapOf(
                "srcdir"            to srcDir,
                "destdir"           to buildDir,
                "includeantruntime" to false,
                "optimize"          to true,
                "deprecation"       to false,
                "debug"             to true
        )) {
            listOf(
                    "gnu/bytecode/",
                    "gnu/commonlisp/lang/",
                    "gnu/ecmascript/",
                    "gnu/expr/",
                    "gnu/kawa/functions/",
                    "gnu/kawa/lispexpr/",
                    "gnu/kawa/reflect/",
                    "gnu/kawa/util/",
                    "gnu/lists/",
                    "gnu/mapping/",
                    "gnu/math/",
                    "gnu/q2/lang/",
                    "gnu/text/",
                    "kawa/lang/",
                    "kawa/standard/",
                    "kawa/repl.java",
                    "kawa/Shell.java",
                    "kawa/Version.java",
                    "kawa/Source*.java",
                    "kawa/Telnet*.java",
                    "gnu/kawa/xml/",
                    "gnu/xml/",
                    "gnu/kawa/models/"
            ).map { withAnt("include", mapOf("name" to it)) }
        }
        println("\n===> Built Kawa core")
    }

    private fun buildLib(srcDir: File, buildDir: File) {
        withAnt("kawac", mapOf(
                "failonerror" to true,
                "destdir" to buildDir,
                "prefix" to "kawa.lib.",
                "modulestatic" to "run",
                "language" to "scheme"
        )) {
            withAnt("arg", mapOf("line" to "--warn-undefined-variable --warn-unknown-member --warn-as-error"))
            withAnt("filelist", mapOf("dir" to srcDir.resolve("kawa/lib"))) {
                listOf(
                        "prim_imports.scm",
                        "prim_syntax.scm",
                        "std_syntax.scm",
                        "reflection.scm",
                        "syntax.scm",
                        "lists.scm",
                        "case_syntax.scm",
                        "DefineRecordType.scm",
                        "ExceptionClasses.scm",
                        "exceptions.scm",
                        "kawa/expressions.scm",
                        "compile_misc.scm",
                        "compile_map.scm",
                        "thread.scm",
                        "characters.scm",
                        "keywords.scm",
                        "numbers.scm",
                        "strings_syntax.scm",
                        "strings.scm",
                        "parameters.scm",
                        "parameterize.scm",
                        "ports.scm",
                        "files.scm",
                        "misc.scm",
                        "misc_syntax.scm",
                        "vectors.scm",
                        "uniform.scm",
                        "bytevectors.scm",
                        "arrays.scm",
                        "system.scm",
                        "kawa/istrings.scm",
                        "kawa/mstrings.scm",
                        "kawa/arglist.scm",
                        "kawa/process-keywords.scm",
                        "kawa/string-cursors.scm",
                        "kawa/hashtable.scm",
                        "kawa/regex.scm",
                        "rnrs/unicode.scm",
                        "scheme/base.scm",
                        "scheme/case-lambda.scm",
                        "scheme/char.scm",
                        "scheme/complex.scm",
                        "scheme/cxr.scm",
                        "scheme/eval.scm",
                        "scheme/file.scm",
                        "scheme/inexact.scm",
                        "scheme/lazy.scm",
                        "scheme/load.scm",
                        "scheme/process-context.scm",
                        "scheme/read.scm",
                        "scheme/repl.scm",
                        "scheme/time.scm",
                        "scheme/write.scm",
                        "scheme/r5rs.scm",
                        "trace.scm"
                ).map { withAnt("file", mapOf("name" to it)) }
            }
        }

        withAnt("kawac", mapOf(
                "failonerror" to true,
                "destdir" to buildDir,
                "prefix" to "kawa.lib.",
                "modulestatic" to "run",
                "language" to "scheme"
        )) {
            withAnt("arg", mapOf("line" to "--warn-undefined-variable --warn-unknown-member --warn-as-error"))
            withAnt("filelist", mapOf("dir" to srcDir.resolve("kawa/lib"))) {
                listOf(
                        "enums.scm",
                        "srfi/8.scm",
                        "srfi/26.scm",
                        "srfi/95.scm",
                        "strings_ext.scm",
                        "rnrs/hashtables.scm",
                        "rnrs/lists.scm",
                        "rnrs/arithmetic/bitwise.scm",
                        "rnrs/sorting.scm",
                        "rnrs/programs.scm",
                        "kawa/base.scm",
                        "kawa/pictures.scm"
                ).map { withAnt("file", mapOf("name" to it)) }
            }
        }

        withAnt("kawac", mapOf(
                "failonerror" to true,
                "destdir" to buildDir,
                "prefix" to "kawa.lib.",
                "modulestatic" to "run",
                "language" to "scheme"
        )) {
            withAnt("arg", mapOf("line" to "--warn-undefined-variable --warn-unknown-member --warn-as-error"))
            withAnt("filelist", mapOf("dir" to srcDir.resolve("kawa/lib"))) {
                listOf(
                        "kawa/quaternions.scm",
                        "kawa/pprint.scm",
                        "kawa/rotations.scm",
                        "kawa/null-5.scm",
                        "kawa/reflect.scm"
                ).map { withAnt("file", mapOf("name" to it)) }
            }
        }
        println("\n===> Built Kawa lib")
    }

    private fun buildSlib(srcDir: File, buildDir: File) {
        withAnt("kawac", mapOf(
                "failonerror" to true,
                "destdir" to buildDir,
                "prefix" to "gnu.kawa.slib.",
                "modulestatic" to "run",
                "language" to "scheme"
        )) {
            withAnt("filelist", mapOf("dir" to srcDir.resolve("gnu/kawa/slib"))) {
                listOf(
                        "readtable.scm",
                        "srfi1.scm",
                        "srfi2.scm",
                        "conditions.scm",
                        "srfi13.scm",
                        "srfi14.scm",
                        "srfi34.scm",
                        "srfi37.scm",
                        "srfi60.scm",
                        "srfi69.scm",
                        "pregexp.scm",
                        "Streams.scm",
                        "StreamsDerived.scm",
                        "StreamsPrimitive.scm",
                        "StreamsType.scm",
                        "genwrite.scm",
                        "pp.scm",
                        "ppfile.scm",
                        "printf.scm",
                        "syntaxutils.scm",
                        "testing.scm",
                        "gui.scm"
                ).map { withAnt("file", mapOf("name" to it)) }
            }
        }
        println("\n===> Built Kawa slib")
    }

    private fun generateJar(srcDir: File, buildDir: File, outDir: File) {
        val services = buildDir.resolve("META-INF/services")
        withAnt("mkdir", mapOf("dir" to services))
        withAnt("echo", mapOf(
                "message" to "kawa.standard.SchemeScriptEngineFactory #Scheme\n",
                "append" to true,
                "file" to services.resolve("javax.script.ScriptEngineFactory")
        ))
        withAnt("jar", mapOf(
                "jarfile" to outDir.resolve("kawa-${extension.version.get()}.jar"),
                "manifest" to srcDir.resolve("jar-manifest")
        )) {
            withAnt("fileset", mapOf("dir" to buildDir)) {
                listOf(
                        "gnu/**/*.class",
                        "kawa/**/*.class",
                        "META-INF/services/*"
                ).map { withAnt("include", mapOf("name" to it)) }
            }
        }
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
}
