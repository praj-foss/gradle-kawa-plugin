/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa.tasks

import groovy.lang.Closure
import org.apache.tools.ant.PropertyHelper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/**
 * Task to configure and build Kawa toolchain.
 */
abstract class KawaConfigure : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @get:InputFile
    abstract val sourceTar: RegularFileProperty

    @get:OutputDirectory
    abstract val distDir: DirectoryProperty

    @Internal
    protected val baseDir: Provider<File> = sourceTar.asFile
            .map { it.resolveSibling("kawa-${version.get()}") }

    @Internal
    protected val toolsDir: Provider<File> = baseDir
            .map { it.resolve("tools") }

    @Internal
    protected val classDir: Provider<File> = baseDir
            .map { it.resolve("classes") }

    @TaskAction
    fun perform() {
        baseDir.get().let {
            if (it.deleteRecursively().not())
                throw GradleException("Directory could not be deleted: ${it.absolutePath}")
        }

        sourceTar.get().asFile.let { tar ->
            project.copy { it
                    .from(project.tarTree(tar))
                    .into(tar.parentFile) }
        }
        logger.debug("Extracted Kawa sources")

        buildTools()
        defineAntTasks()
        preprocessClasses()
        generateVectorSources()
        buildCore()
        buildLib()
        buildSlib()
        generateJars()
    }

    private fun buildTools() {
        val src  = baseDir.get()
        val dest = toolsDir.get()

        withAnt("mkdir", mapOf("dir" to dest))
        withAnt("javac", mapOf(
                "srcdir"            to src,
                "destdir"           to dest,
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
        logger.debug("Built Kawa Ant tools")
    }

    private fun defineAntTasks() {
        val tools = toolsDir.get()

        withAnt("taskdef", mapOf(
                "name"      to "kawac",
                "classname" to "gnu.kawa.ant.Kawac",
                "classpath" to tools
        ))
        withAnt("taskdef", mapOf(
                "name"      to "xcopy",
                "classname" to "gnu.kawa.ant.XCopy",
                "classpath" to tools
        ))
        logger.debug("Defined kawac and xcopy Ant tasks")
    }

    private fun preprocessClasses() {
        val src = baseDir.get()
        withAnt("mkdir", mapOf("dir" to src))
        withAnt("xcopy", mapOf(
                "todir" to src.resolve("kawa"),
                "overwrite" to false
        )) {
            withAnt("fileset", mapOf(
                    "dir" to src.resolve("kawa"),
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
                        "value" to "\"${version.get()}\""))
            }
        }
        logger.debug("Finished preprocessing sources")
    }

    private fun generateVectorSources() {
        val src = baseDir.get()
        val tools = toolsDir.get()

        fun makePrimeVectorSource(tag: String, out: String) =
                withAnt("java", mapOf("classname" to "gnu.kawa.util.PreProcess")) {
                    withAnt("classpath", mapOf("location" to tools))
                    listOf(
                            "%${tag}",
                            "%UniformVector",
                            "-o",
                            src.resolve("gnu/lists/${out}Vector.java"),
                            src.resolve("gnu/lists/PrimVector.template")
                    ).map { withAnt("arg", mapOf("value" to it)) }
                }

        val vectorTypes = arrayListOf(
                "F", "Bit", "Byte", "Short", "Int", "Long", "F32", "F64",
                "S8", "S16", "S32", "S64", "U8", "U16", "U32", "U64")

        // Preprocess files unless they're up to date
        val upToDate = "simplevector-files-uptodate"
        withAnt("uptodate", mapOf("property" to upToDate)) {
            withAnt("srcfiles", mapOf(
                    "file" to src.resolve("gnu/lists/PrimVector.template")))
            withAnt("srcfiles", mapOf(
                    "file" to tools.resolve("gnu/kawa/util/PreProcess.class")))
            withAnt("compositemapper") {
                vectorTypes.map { src.resolve("gnu/lists/${it}Vector.java") }
                        .map { withAnt("mergemapper", mapOf("to" to it)) }
            }
        }

        if (PropertyHelper.getPropertyHelper(ant.antProject)
                        .testUnlessCondition(upToDate)) {
            makePrimeVectorSource("OBJECT", vectorTypes[0])
            vectorTypes.drop(1)
                    .map { makePrimeVectorSource(it.toUpperCase(Locale.ENGLISH), it) }
        }
        logger.debug("Generated Vector sources")
    }

    private fun buildCore() {
        val src = baseDir.get()
        val classes = classDir.get()

        withAnt("mkdir", mapOf("dir" to classes))
        withAnt("javac", mapOf(
                "srcdir"            to src,
                "destdir"           to classes,
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
        logger.debug("Built Kawa core")
    }

    private fun buildLib() {
        val src = baseDir.get()
        val classes = classDir.get()

        withAnt("kawac", mapOf(
                "failonerror" to true,
                "destdir" to classes,
                "prefix" to "kawa.lib.",
                "modulestatic" to "run",
                "language" to "scheme"
        )) {
            withAnt("arg", mapOf("line" to "--warn-undefined-variable --warn-unknown-member --warn-as-error"))
            withAnt("filelist", mapOf("dir" to src.resolve("kawa/lib"))) {
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
                "destdir" to classes,
                "prefix" to "kawa.lib.",
                "modulestatic" to "run",
                "language" to "scheme"
        )) {
            withAnt("arg", mapOf("line" to "--warn-undefined-variable --warn-unknown-member --warn-as-error"))
            withAnt("filelist", mapOf("dir" to src.resolve("kawa/lib"))) {
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
                "destdir" to classes,
                "prefix" to "kawa.lib.",
                "modulestatic" to "run",
                "language" to "scheme"
        )) {
            withAnt("arg", mapOf("line" to "--warn-undefined-variable --warn-unknown-member --warn-as-error"))
            withAnt("filelist", mapOf("dir" to src.resolve("kawa/lib"))) {
                listOf(
                        "kawa/quaternions.scm",
                        "kawa/pprint.scm",
                        "kawa/rotations.scm",
                        "kawa/null-5.scm",
                        "kawa/reflect.scm"
                ).map { withAnt("file", mapOf("name" to it)) }
            }
        }
        logger.debug("Built Kawa lib")
    }

    private fun buildSlib() {
        val src = baseDir.get()
        val classes = classDir.get()

        withAnt("kawac", mapOf(
                "failonerror" to true,
                "destdir" to classes,
                "prefix" to "gnu.kawa.slib.",
                "modulestatic" to "run",
                "language" to "scheme"
        )) {
            withAnt("filelist", mapOf("dir" to src.resolve("gnu/kawa/slib"))) {
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
        logger.debug("Built Kawa slib")
    }

    private fun generateJars() {
        val ver = version.get()
        val src = baseDir.get()
        val classes = classDir.get()
        val tools = toolsDir.get()
        val out = distDir.get().asFile

        val services = classes.resolve("META-INF/services")
        withAnt("mkdir", mapOf("dir" to services))

        // Kawa core
        withAnt("echo", mapOf(
                "message" to "kawa.standard.SchemeScriptEngineFactory #Scheme\n",
                "append" to true,
                "file" to services.resolve("javax.script.ScriptEngineFactory")
        ))
        withAnt("jar", mapOf(
                "destfile" to out.resolve("kawa-${ver}.jar"),
                "manifest" to src.resolve("jar-manifest"),
                "update" to true
        )) {
            withAnt("fileset", mapOf("dir" to classes)) {
                listOf(
                        "gnu/**/*.class",
                        "kawa/**/*.class",
                        "META-INF/services/*"
                ).map { withAnt("include", mapOf("name" to it)) }
            }
        }

        // Kawa Ant tools
        withAnt("jar", mapOf(
                "destfile" to out.resolve("kawa-ant-${ver}.jar"),
                "update" to true
        )) {
            withAnt("fileset", mapOf("dir" to tools)) {
                withAnt("include", mapOf("name" to "gnu/kawa/ant/*.class"))
            }
        }
        logger.debug("Generated Kawa jars")
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
