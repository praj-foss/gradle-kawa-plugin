/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import java.io.File

/**
 * Stores plugin-wide settings. This object should be instantiated
 * after build script evaluation to defer fetching values from the
 * extension object.
 */
internal class KawaSettings(
        project: Project,
        kawaExt: KawaExtension,
        kawacExt: KawacExtension
) {
    val version: String = kawaExt.version
            ?: throw InvalidUserDataException("${KAWA_EXTENSION}.version must not be null")

    val cacheDir: File = kawaExt.cacheDir
            ?.let { project.file(it) }
            ?: throw InvalidUserDataException("${KAWA_EXTENSION}.cacheDir must not be null")

    val kawacSrcDir: File = kawacExt.srcDir
            ?.let { project.file(it) }
            ?: throw InvalidUserDataException("${KAWA_EXTENSION}.${KAWAC_EXTENSION}.srcDir must not be null")

    val kawacDestDir: File = kawacExt.destDir
            ?.let { project.file(it) }
            ?: throw InvalidUserDataException("${KAWA_EXTENSION}.${KAWAC_EXTENSION}.destDir must not be null")

    val kawacLanguage: String = kawacExt.language
            ?: throw  InvalidUserDataException("${KAWA_EXTENSION}.${KAWAC_EXTENSION}.language must not be null")

    val kawacArgs: String = kawacExt.args
            ?: throw  InvalidUserDataException("${KAWA_EXTENSION}.${KAWAC_EXTENSION}.args must not be null")

    val baseDir   = cacheDir.resolve(version)

    val sourceTar = baseDir.resolve("kawa-${version}.tar.gz")

    val distDir   = baseDir.resolve("dist")
}