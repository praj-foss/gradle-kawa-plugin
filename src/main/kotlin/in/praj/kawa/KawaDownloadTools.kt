/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Task to download kawa tools.
 */
open class KawaDownloadTools : DefaultTask() {
    @Input
    val version: Property<String> = project.extensions.getByType(KawaExtension::class.java).version

    @TaskAction
    fun perform() {
        // TODO: Implement this
        println("Downloading version ${version.get()}")
    }
}