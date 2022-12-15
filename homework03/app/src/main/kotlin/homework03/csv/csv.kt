package homework03.csv

import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.stream.writeString
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

suspend fun write(csv: String, path: String, filename: String) {
    val fileVfs = localVfs(path)
    fileVfs[filename].delete()
    val file = fileVfs[filename].open(VfsOpenMode.CREATE)
    file.writeString(csv)
    file.close()
}

fun <T : Any> csvSerialize(data: Iterable<T>, klass: KClass<T>) = buildString { serializeObject(data, klass) }

private val primitiveTypes = listOf(
    Int::class, Short::class, Long::class, Byte::class, Float::class, Double::class,
    Boolean::class, Char::class
)

private fun <T : Any> StringBuilder.serializeObject(data: Iterable<T>, klass: KClass<T>) {
    serializeHeader(klass)
    append("\n")

    if (data.any {
            it.javaClass.kotlin != klass
        }) throw IllegalArgumentException("not all types match")

    data.forEach {
        serializeObject(it)
        append("\n")
    }
}

private fun StringBuilder.serializeNumber(value: Any) = apply {
    append(value)
}

private fun StringBuilder.serializeValue(value: Any) = apply {
    val kClass = value.javaClass.kotlin
    when (kClass) {
        String::class -> {
            serializeString(value as String)
        }

        else -> {
            serializeNumber(value)
        }
    }
}

private fun StringBuilder.serializeString(value: String) = apply {
    append('"')
    append(value)
    append('"')
}

private fun <T : Any> StringBuilder.serializeHeader(klass: KClass<T>) = apply {
    append("")
    val properties = klass.memberProperties

    when (klass) {
        String::class, in primitiveTypes -> {
            serializeString("value")
        }

        else -> {
            properties.joinTo(this, ",") { p ->
                serializeString(p.name)
                ""
            }
        }
    }
}

private fun StringBuilder.serializeObject(value: Any) {
    val kClass = value.javaClass.kotlin
    val properties = kClass.memberProperties

    when (kClass) {
        String::class -> {
            serializeString(value as String)
        }

        in primitiveTypes -> serializeNumber(value)
        else -> {
            properties.joinTo(this, ",") { p ->
                serializeValue(p.get(value) ?: throw IllegalArgumentException())
                ""
            }
        }
    }
}