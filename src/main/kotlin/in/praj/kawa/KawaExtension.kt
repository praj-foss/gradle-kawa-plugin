/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Configuration options for the Kawa plugin.
 */
open class KawaExtension @Inject constructor(
        objects: ObjectFactory
) {
    val version: Property<String> = objects.property(String::class.java)
    val kawaBuildDir: DirectoryProperty = objects.directoryProperty()
}