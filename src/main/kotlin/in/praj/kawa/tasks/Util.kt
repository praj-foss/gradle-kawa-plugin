/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package `in`.praj.kawa.tasks

import groovy.lang.Closure
import org.gradle.api.Task

/**
 * Common utilities for custom tasks.
 */

fun Task.withAnt(
        name: String,
        props: Map<String, Any>? = null,
        closure: (() -> Any)? = null
): Any =
        if (props == null && closure == null)
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