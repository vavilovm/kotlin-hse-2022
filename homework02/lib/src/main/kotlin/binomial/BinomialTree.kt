package binomial

interface SelfMergeable<T> {
    operator fun plus(other: T): T
}

/*
 * BinomialTree - реализация биномиального дерева
 *
 * Вспомогательная структура для биномиальной кучи
 * https://en.wikipedia.org/wiki/Binomial_heap
 *
 * Запрещено использовать
 *
 *  - var
 *  - циклы
 *  - стандартные коллекции
 *
 * Детали внутренней реализации должны быть спрятаны
 * Создание - только через single() и plus()
 *
 * Дерево совсем без элементов не предусмотрено
 */

class BinomialTree<T : Comparable<T>> private constructor(val value: T, val children: FList<BinomialTree<T>?>) :
    SelfMergeable<BinomialTree<T>> {
    // порядок дерева
    val order: Int = children.size

    /*
     * слияние деревьев
     * При попытке слить деревья разных порядков, нужно бросить IllegalArgumentException
     *
     * Требуемая сложность - O(1)
     */
    override fun plus(other: BinomialTree<T>): BinomialTree<T> {
        if (this.order != other.order) throw IllegalArgumentException()

        return if (this.value <= other.value) BinomialTree(this.value, FList.Cons(other, this.children))
        else BinomialTree(other.value, FList.Cons(this, other.children))
    }

    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialTree<T> = BinomialTree(value, FList.nil())
    }
}
