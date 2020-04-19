/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Configuration options for the Kawa plugin.
 */
open class KawaExtension(
        project: Project
) {
    val version: Property<String> = project.objects.property(String::class.java)
    val kawaBuildDir: Provider<Directory> = project.layout.buildDirectory.dir("kawa")
}