package kennarddh.genesis.core.timers.annotations

/**
 * Set [repeatCount] to -1 to make it run infinitely
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class TimerTask(val delaySeconds: Float, val intervalSeconds: Float = 0f, val repeatCount: Int = -1)
