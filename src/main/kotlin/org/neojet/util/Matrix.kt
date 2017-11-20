package org.neojet.util

import kotlin.coroutines.experimental.buildIterator

/**
 * @author dhleong
 */

class Matrix<T> (val rows: Int, val cols: Int, val array: Array<Array<T>>) {

    companion object {

        inline operator fun <reified T> invoke(rows: Int, cols: Int) =
            Matrix(rows, cols, Array(rows, { arrayOfNulls<T>(cols) }))

        inline operator fun <reified T> invoke(rows: Int, cols: Int, operator: (Int, Int) -> (T)): Matrix<T> {
            val array = Array(rows, {
                val row = it
                Array(cols, { operator(row, it) })
            })
            return Matrix(rows, cols, array)
        }
    }

    operator fun get(row: Int): Array<T> {
        return array[row]
    }

    operator fun get(row: Int, col: Int): T {
        return array[row][col]
    }

    operator fun set(row: Int, col: Int, t: T) {
        array[row][col] = t
    }

    operator fun set(row: Int, contents: Array<T>) {
        if (contents.size != array[row].size) {
            throw IllegalArgumentException()
        }

        for (col in 0 until cols) {
            array[row][col] = contents[col]
        }
    }

    inline fun <reified R> resizeTo(newRows: Int, newCols: Int, operator: (Int, Int) -> (R)): Matrix<R> {
        // TODO reuse arrays if possible
        val result = Matrix(newRows, newCols, operator)
        val copyRows = minOf(rows, newRows)
        val copyCols = minOf(cols, newCols)
        for (y in 0 until copyRows) {
            System.arraycopy(array[y], 0, result.array[y], 0, copyCols)

            for (x in copyCols until newCols) {
                result.array[y][x] = operator(y, x)
            }
        }
        return result
    }

    fun filter(predicate: (T) -> Boolean) = buildIterator {
        for (y in 0 until rows) {
            @Suppress("LoopToCallChain")
            for (x in 0 until cols) {
                val item = this@Matrix[y, x]
                if (predicate(item)) {
                    yield(item)
                }
            }
        }
    }
}
