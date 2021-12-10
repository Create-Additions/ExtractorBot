package handy.base

/**
 * [init] is called once [Handy's Discord bot][handy.HandyDiscord] is initialized,
 * this exists to make the code cleaner by not having to call a bunch static init methods
 *
 * To subscribe an initable annotate with [SubscribeInitable], you **NEED** a constructor with no parameters, otherwise Handy will crash
 */
interface Initable {
    fun init()
}