package handy

import handy.base.Subscribable
import handy.base.Subscribe
import org.reflections.Reflections
import java.lang.IllegalStateException

object HandySubscriptions {
    lateinit var subscribables: Set<Class<*>>;

    fun findAndSubscribe() {
        subscribables = Reflections("handy").getTypesAnnotatedWith(Subscribe::class.java)
        subscribables.forEach {
            val i = it.getConstructor().newInstance()
            if(i is Subscribable) {
                i.subscribe()
            } else {
                throw IllegalStateException("Class ${it.simpleName} is annotated with @Subscribe but is not a subscribable \n${it.name}")
            }
        }
    }
}