interface NDArray : SizeAware, DimentionAware {
    /*
     * Получаем значение по индексу point
     *
     * Если размерность point не равна размерности NDArray
     * бросаем IllegalPointDimensionException
     *
     * Если позиция по любой из размерностей некорректна с точки зрения
     * размерности NDArray, бросаем IllegalPointCoordinateException
     */
    fun at(point: Point): Int

    /*
     * Устанавливаем значение по индексу point
     *
     * Если размерность point не равна размерности NDArray
     * бросаем IllegalPointDimensionException
     *
     * Если позиция по любой из размерностей некорректна с точки зрения
     * размерности NDArray, бросаем IllegalPointCoordinateException
     */
    fun set(point: Point, value: Int)

    /*
     * Копируем текущий NDArray
     *
     */
    fun copy(): NDArray

    /*
     * Создаем view для текущего NDArray
     *
     * Ожидается, что будет создан новая реализация интерфейса.
     * Но она не должна быть видна в коде, использующем эту библиотеку как внешний артефакт
     *
     * Должна быть возможность делать view над view.
     *
     * In-place-изменения над view любого порядка видна в оригинале и во всех view
     *
     * Проблемы thread-safety игнорируем
     */
    fun view(): NDArray

    /*
     * In-place сложение
     *
     * Размерность other либо идентична текущей, либо на 1 меньше
     * Если она на 1 меньше, то по всем позициям, кроме "лишней", она должна совпадать
     *
     * Если размерности совпадают, то делаем поэлементное сложение
     *
     * Если размерность other на 1 меньше, то для каждой позиции последней размерности мы
     * делаем поэлементное сложение
     *
     * Например, если размерность this - (10, 3), а размерность other - (10), то мы для три раза прибавим
     * other к каждому срезу последней размерности
     *
     * Аналогично, если размерность this - (10, 3, 5), а размерность other - (10, 5), то мы для пять раз прибавим
     * other к каждому срезу последней размерности
     */
    fun add(other: NDArray)

    /*
     * Умножение матриц. Immutable-операция. Возвращаем NDArray
     *
     * Требования к размерности - как для умножения матриц.
     *
     * this - обязательно двумерная
     *
     * other - может быть двумерной, с подходящей размерностью, равной 1 или просто вектором
     *
     * Возвращаем новую матрицу (NDArray размерности 2)
     *
     */
    fun dot(other: NDArray): NDArray
}

/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val shape: Shape, private val array: IntArray) : NDArray {
    companion object {
        fun zeros(shape: Shape): NDArray = DefaultNDArray(shape, IntArray(shape.size))
        fun ones(shape: Shape): NDArray = DefaultNDArray(shape, IntArray(shape.size) { 1 })
    }

    override val size: Int = shape.size
    override val ndim: Int = shape.ndim
    override fun dim(i: Int) = shape.dim(i)

    private val multipliers = IntArray(ndim).apply {
        var mult = 1
        for (d in shape.ndim - 1 downTo 0) {
            this[d] = mult
            mult *= shape.dim(d)
        }
    }

    private fun Point.toIndex(): Int {
        // array(10,100), a[1][2] -> 2 + 1*100
        if (ndim != shape.ndim) {
            throw NDArrayException.IllegalPointDimensionException()
        }
        var index = 0
        for (d in shape.ndim - 1 downTo 0) {
            val pointDim = dim(d)
            val shapeDim = shape.dim(d)
            if (pointDim < 0 || shapeDim < pointDim) {
                throw NDArrayException.IllegalPointCoordinateException()
            }

            index += pointDim * multipliers[d]
        }
        return index
    }

    private fun Int.toPoint(): Point {
        var i = this
        val point = mutableListOf<Int>()

        for (d in 0 until ndim) {
            point.add(i / multipliers[d])
            i -= point.last() * multipliers[d]
        }
        return DefaultPoint(*point.toIntArray())
    }

    override fun at(point: Point): Int = array[point.toIndex()]

    override fun set(point: Point, value: Int) {
        array[point.toIndex()] = value
    }

    override fun copy(): NDArray {
        return DefaultNDArray(shape, array.clone())
    }

    override fun view(): NDArray {
        return DefaultNDArray(shape, array)
    }

    override fun add(other: NDArray) {
        if (other.ndim != ndim && other.ndim + 1 != ndim) {
            throw NDArrayException.IllegalAddDimensions()
        }
        for (i in 0 until other.ndim) {
            if (dim(i) != other.dim(i)) {
                throw NDArrayException.IllegalAddShape()
            }
        }

        if (other.ndim == ndim) {
            for (i in 0 until other.size) {
                array[i] += other.at(i.toPoint())
            }
            return
        }


        val lastDim = other.dim(other.ndim - 1)
        for (i in 0 until other.size) {
            val curPoint = (i * lastDim).toPoint().reduceDimensions(other.ndim)
            val elem = other.at(curPoint)
            for (j in i * lastDim until (i + 1) * lastDim) {
                array[j] += elem
            }
        }
    }

    private fun NDArray.at2d(i: Int, j: Int): Int {
        return if (ndim == 2) {
            at(DefaultPoint(i, j))
        } else {
            at(DefaultPoint(i))
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (this.ndim != 2 || other.ndim < 1 || other.ndim > 2 || other.dim(0) != this.dim(1)) {
            throw NDArrayException.IllegalDotDimensions()
        }

        if (other.size == 1) {
            val mult = other.at(DefaultPoint(0))
            return DefaultNDArray(shape, array.map { it * mult }.toIntArray())
        }

        val otherColsNum = if (other.ndim == 2) other.dim(1) else 1
        val result = zeros(DefaultShape(dim(1), otherColsNum))

        for (i in 0 until this.dim(0)) {
            for (j in 0 until otherColsNum) {
                val sum = (0 until this.dim(1)).sumOf { k ->
                    this.at2d(i, k) * other.at2d(k, j)
                }

                result.set(DefaultPoint(i, j), sum)
            }
        }
        return result
    }

}

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException : NDArrayException()
    class IllegalPointDimensionException : NDArrayException()

    class IllegalAddDimensions : NDArrayException()

    class IllegalAddShape : NDArrayException()
    class IllegalDotDimensions : NDArrayException()
}