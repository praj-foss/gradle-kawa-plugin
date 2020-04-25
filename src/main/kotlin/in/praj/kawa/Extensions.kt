/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

/**
 * Extension objects for the Kawa plugin. Using these extensions
 * is the recommended way of configuring the plugin from the
 * build script.
 */
interface KawaExtension {
    var version: String?
    var cacheDir: Any?
}

interface KawacExtension {
    var srcDir: Any?
    var destDir: Any?
    var language: String?
    var args: String?
}
