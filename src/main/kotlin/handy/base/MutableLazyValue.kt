package handy.base

class MutableLazyValue<T>(constructor: () -> T) : LazyValue<T>(constructor) {
    fun set(value: T) {
        this.value = value
    }

    fun set(constructor: () -> T) {
        this.constructor = constructor
        this.value = null
    }
}