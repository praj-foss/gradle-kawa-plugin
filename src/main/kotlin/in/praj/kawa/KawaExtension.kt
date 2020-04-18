/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Configuration options for the Kawa plugin.
 */
open class KawaExtension(
        project: Project
) {
    val version: Property<String> = project.objects.property(String::class.java)
}