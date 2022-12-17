package binomial

/*
 * BinomialHeap - реализация биномиальной кучи
 *
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
 * Куча совсем без элементов не предусмотрена
 *
 * Операции
 *
 * plus с кучей
 * plus с элементом
 * top - взятие минимального элемента
 * drop - удаление минимального элемента
 */
class BinomialHeap<T : Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>?>) :
    SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T : Comparable<T>> single(value: T) = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        return BinomialHeap(mergeHeaps(this.trees, other.trees, FList.nil()).reverse())
    }


    /*
     * добавление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T) = plus(single(elem))

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top() = trees.filter { it != null }.map { it as BinomialTree<T> }.run {
        fold(first().value) { a: T, b: BinomialTree<T> -> minOf(a, b.value) }
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minTree = top().let { min ->
            (trees.filter { it?.value == min } as FList.Cons).head ?: throw NoSuchElementException()
        }

        return BinomialHeap(trees.map { if (it == minTree) null else it }) + BinomialHeap(minTree.children.reverse())
    }


    private fun mergeTrees(
        tree1: BinomialTree<T>?, tree2: BinomialTree<T>?, carry: BinomialTree<T>? = null
    ): Pair<BinomialTree<T>?, BinomialTree<T>?> = when {
        (tree1 == null && tree2 == null) -> null to carry
        (tree1 == null) -> {
            if (carry != null) tree2!! + carry to null
            else null to tree2

        }

        (tree2 == null) -> {
            if (carry != null) tree1 + carry to null
            else null to tree1
        }

        else -> tree1 + tree2 to carry
    }

    private tailrec fun mergeHeaps(
        heap1: FList<BinomialTree<T>?>,
        heap2: FList<BinomialTree<T>?>,
        res: FList<BinomialTree<T>?>,
        carry: BinomialTree<T>? = null
    ): FList<BinomialTree<T>?> {
        if (heap1.size == 0 && heap2.size == 0 && carry == null) return res
        val (newCarry, head) = mergeTrees(heap1.head(), heap2.head(), carry)
        return mergeHeaps(heap1.tail(), heap2.tail(), FList.Cons(head, res), newCarry)
    }
}
