/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.Project

/**
 * Stores plugin-wide settings. This object should be instantiated
 * after build script evaluation to defer fetching values from the
 * extension object.
 */
internal class KawaSettings(
        project: Project,
        extension: KawaExtension
) {
    val version   = extension.version!!
    val cacheDir  = project.file(extension.cacheDir!!)
    val baseDir   = cacheDir.resolve(version)
    val sourceTar = baseDir.resolve("kawa-${version}.tar.gz")
    val distDir   = baseDir.resolve("dist")
}