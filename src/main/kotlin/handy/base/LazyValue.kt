package handy.base

open class LazyValue<T>(protected var constructor: () -> T) {
    protected var value: T? = null

    fun get(): T {
        if(value == null) {
            value = constructor()
        }
        return value!!
    }
}