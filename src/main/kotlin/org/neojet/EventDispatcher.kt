package org.neojet

import io.neovim.java.event.Event

/**
 * @author dhleong
 */

annotation class HandlesEvent

internal typealias EventHandler = (Any) -> Unit

class EventDispatcher(val target: Any, val warnUnhandled: Boolean = true) {

    private val handlers = HashMap<Class<*>, EventHandler>().let { handlers ->
        target.javaClass.declaredMethods
            .filter { it.getAnnotation(HandlesEvent::class.java) != null }
            .forEach { method ->
                val parameterTypes = method.parameterTypes
                if (method.parameterTypes.size != 1) {
                    throw IllegalArgumentException(
                        "On @HandlesEvent method $method; incorrect " +
                        "number of parameters (${method.parameterTypes.size})")
                }

                val parameterType = parameterTypes[0]
                if (!Event::class.java.isAssignableFrom(parameterType)) {
                    throw IllegalArgumentException(
                        "On @HandlesEvent method $method; parameter " +
                        "type $parameterType is not an Event")
                }

                handlers[parameterType] = { event ->
                    method.invoke(target, event)
                }
            }

        // return our inflated map as the result of the initialization
        handlers
    }

    fun <T : Event<*>> dispatch(event: T) {
        handlers[event.javaClass]?.let {
            it.invoke(event)
            return
        }

        if (warnUnhandled) {
            System.err.println("WARN: Unhandled event on $target: $event")
        }
    }

}
