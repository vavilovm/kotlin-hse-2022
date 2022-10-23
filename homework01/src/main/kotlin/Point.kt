interface Point : DimentionAware

/**
 * Реализация Point по умолчанию
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количеством параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultPoint(vararg dimensions: Int) : Point {
    private val dimensions = intArrayOf(*dimensions)
    override val ndim: Int = dimensions.size
    override fun dim(i: Int) = dimensions[i]
}


fun Point.reduceDimensions(resultDimensions: Int): Point {
    val dims = mutableListOf<Int>()
    for (i in 0 until resultDimensions) {
        dims.add(dim(i))
    }
    return DefaultPoint(*dims.toIntArray())
}