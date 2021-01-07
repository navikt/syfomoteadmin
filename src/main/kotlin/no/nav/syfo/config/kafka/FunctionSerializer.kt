package no.nav.syfo.config.kafka

import org.apache.kafka.common.serialization.Serializer
import java.util.function.Function

class FunctionSerializer<T>(
    private val serializer: Function<T, ByteArray>
) : Serializer<T> {
    override fun configure(configs: Map<String?, *>, isKey: Boolean) {}

    override fun serialize(topic: String, t: T): ByteArray {
        return serializer.apply(t)
    }

    override fun close() {}
}
