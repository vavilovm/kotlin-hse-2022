interface Shape : DimentionAware, SizeAware

/**
 * Реализация Shape по умолчанию
 *
 * Должны работать вызовы DefaultShape(10), DefaultShape(12, 3), DefaultShape(12, 3, 12, 4, 56)
 * с любым количеством параметров
 *
 * При попытке создать пустой Shape бросается EmptyShapeException
 *
 * При попытке указать неположительное число по любой размерности бросается NonPositiveDimensionException
 * Свойство index - минимальный индекс с некорректным значением, value - само значение
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultShape(vararg dimensions: Int) : Shape {
    private val dimensions = intArrayOf(*dimensions)
    init {
        dimensions.withIndex().firstOrNull { it.value <= 0}?.let {
            throw ShapeArgumentException.NonPositiveDimensionException(it.index, it.value)
        }
        if (dimensions.isEmpty()) throw ShapeArgumentException.EmptyShapeException()
    }
    override val size: Int = dimensions.reduce(Int::times)
    override val ndim: Int = dimensions.size

    override fun dim(i: Int) = dimensions[i]
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
     class EmptyShapeException : ShapeArgumentException("Shape is empty")
     class NonPositiveDimensionException(index: Int, value: Int) : ShapeArgumentException("Dimension $index value $value is not positive")
}
