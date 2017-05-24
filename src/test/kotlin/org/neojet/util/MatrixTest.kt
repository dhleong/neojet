package org.neojet.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * @author dhleong
 */
class MatrixTest {

    @Test fun resizeDown() {
        val old = Matrix(4, 8, { row, col -> row * 8 + col })
        val new = old.resizeTo(2, 4, { _,_ -> -1 })

        assertThat(new.cols).isEqualTo(4)
        assertThat(new.array[0])
            .containsExactly(0, 1, 2, 3)
    }

    @Test fun resizeSame() {
        val old = Matrix(2, 4, { row, col -> row * 4 + col })
        val new = old.resizeTo(2, 4, { _,_ -> -1 })

        assertThat(new.cols).isEqualTo(4)
        assertThat(new.array[0])
            .containsExactly(0, 1, 2, 3)
    }

    @Test fun resizeUp() {
        val old = Matrix(2, 4, { row, col -> row * 4 + col })
        val new = old.resizeTo(4, 8, { _,_ -> -1 })

        assertThat(new.cols).isEqualTo(8)
        assertThat(new.array[0])
            .containsExactly(0, 1, 2, 3, -1, -1, -1, -1)
    }

    @Test fun resizeRowsUp_ColsDown() {
         val old = Matrix(2, 4, { row, col -> row * 4 + col })
        val new = old.resizeTo(4, 2, { _,_ -> -1 })

        assertThat(new.cols).isEqualTo(2)
        assertThat(new.array[0])
            .containsExactly(0, 1)
    }
}