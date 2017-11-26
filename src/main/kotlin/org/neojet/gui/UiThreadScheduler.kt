package org.neojet.gui

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import java.awt.event.ActionListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingUtilities
import javax.swing.Timer

class UiThreadScheduler : Scheduler() {
    companion object {
        val instance: UiThreadScheduler by lazy { UiThreadScheduler() }
    }

    override fun scheduleDirect(run: Runnable): Disposable {
        val scheduled = ScheduledRunnable(
                RxJavaPlugins.onSchedule(run)
        )
        SwingUtilities.invokeLater(scheduled)
        return scheduled
    }

    override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
        val timed = TimedRunnable(
                RxJavaPlugins.onSchedule(run),
                unit.toMillis(delay).toInt()
        )
        timed.timer.start()
        return timed
    }

    inline fun scheduleDirect(delay: Long, unit: TimeUnit, crossinline block: () -> Unit): Disposable {
        return scheduleDirect({
            block()
        }, delay, unit)
    }

    override fun createWorker(): Worker = SwingWorker(this)


    class SwingWorker(private val scheduler: UiThreadScheduler) : Worker() {
        private var disposed = false

        override fun isDisposed(): Boolean = disposed

        override fun schedule(run: Runnable, delay: Long, unit: TimeUnit): Disposable =
                scheduler.scheduleDirect(run, delay, unit)

        override fun dispose() {
            disposed = true
        }

    }

    open class ScheduledRunnable(actual: Runnable) : Runnable, Disposable {
        private var actual = AtomicReference<Runnable>(actual)

        override fun run() {
            try {
                actual.getAndSet(null)?.run()
            } catch (e: Exception) {
                Exceptions.throwIfFatal(e)
                RxJavaPlugins.onError(e)
            }
        }

        override fun isDisposed(): Boolean = actual.get() == null

        override fun dispose() {
            actual.set(null)
        }
    }

    class TimedRunnable(
            actual: Runnable,
            delayMillis: Int,
            periodic: Boolean = false,
            initialDelayMillis: Int = -1
    ) : ScheduledRunnable(actual) {

        private val actionListener: ActionListener = ActionListener {
            run()

            if (!periodic) {
                timer.stop()
            }
        }
        val timer = Timer(delayMillis, actionListener)

        init {
            if (initialDelayMillis >= 0) {
                timer.initialDelay = initialDelayMillis
            }
        }

        override fun dispose() {
            super.dispose()
            timer.stop()
        }
    }
}