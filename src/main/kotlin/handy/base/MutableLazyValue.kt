package handy.base

import java.util.*

class MutableLazyValue<T>(constructor: () -> T) : LazyValue<T>(constructor) {
    fun set(value: T) {
        this.value = Optional.of(value)
    }

    fun set(constructor: () -> T) {
        this.constructor = constructor
        this.value = Optional.empty()
    }
}