/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa

/**
 * Extension object for the Kawa plugin. Using this extension
 * is the recommended way of configuring the plugin from the
 * build script.
 */
interface KawaExtension {
    var version: String
    var cacheDir: Any
}