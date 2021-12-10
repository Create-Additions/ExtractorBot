package handy

import handy.base.Initable
import handy.base.SubscribeInitable
import org.reflections.Reflections
import java.lang.IllegalStateException

/**
 * See [Initable]
 */
object HandyInitables {
    lateinit var initables: Set<Class<*>>;

    fun findAndInit() {
        initables = Reflections("handy").getTypesAnnotatedWith(SubscribeInitable::class.java)
        initables.forEach {
            println("Running initable ${it.name}")
            val i = it.kotlin.objectInstance ?: it.getConstructor().newInstance()
            if(i is Initable) {
                i.init()
            } else {
                throw IllegalStateException("Class ${it.simpleName} is annotated with @SubscribeInitable but is not an initable \n${it.name}")
            }
        }
    }
}