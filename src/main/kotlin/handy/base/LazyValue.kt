package handy.base

import java.util.*

open class LazyValue<T>(protected var constructor: () -> T) {
    protected var value = Optional.empty<T>() // value needs to be wrapped so kotlin stops yelling at me for nullables

    fun get(): T {
        if(!value.isPresent) {
            value = Optional.of(constructor()!!)
        }
        return value.get()
    }
}