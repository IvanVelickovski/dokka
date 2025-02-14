package org.jetbrains.dokka

import org.jetbrains.dokka.utilities.DokkaLogger
import java.util.concurrent.atomic.AtomicInteger

import java.util.function.BiConsumer

/**
 * Accessed with reflection
 */
@Suppress("unused")
class DokkaBootstrapImpl : DokkaBootstrap {

    class DokkaProxyLogger(val consumer: BiConsumer<String, String>) : DokkaLogger {
        private val warningsCounter = AtomicInteger()
        private val errorsCounter = AtomicInteger()

        override var warningsCount: Int
            get() = warningsCounter.get()
            set(value) = warningsCounter.set(value)

        override var errorsCount: Int
            get() = errorsCounter.get()
            set(value) = errorsCounter.set(value)

        override fun debug(message: String) {
            consumer.accept("debug", message)
        }

        override fun info(message: String) {
            consumer.accept("info", message)
        }

        override fun progress(message: String) {
            consumer.accept("progress", message)
        }

        override fun warn(message: String) {
            consumer.accept("warn", message).also { warningsCounter.incrementAndGet() }
        }

        override fun error(message: String) {
            consumer.accept("error", message).also { errorsCounter.incrementAndGet() }
        }
    }

    private lateinit var generator: DokkaGenerator

    fun configure(logger: DokkaLogger, configuration: DokkaConfigurationImpl) {
        generator = DokkaGenerator(configuration, logger)
    }

    override fun configure(serializedConfigurationJSON: String, logger: BiConsumer<String, String>) = configure(
        DokkaProxyLogger(logger),
        DokkaConfigurationImpl(serializedConfigurationJSON)
    )

    override fun generate() = generator.generate()
}
