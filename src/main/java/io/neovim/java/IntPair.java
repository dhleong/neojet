package io.neovim.java;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Used to represent a window dimension or
 *  a cursor position, for example
 *
 * @author dhleong
 */
@JsonFormat(shape=JsonFormat.Shape.ARRAY)
public class IntPair {
    public final int first;
    public final int second;

    public IntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int row() {
        return first;
    }

    public int col() {
        return second;
    }


    public int x() {
        return first;
    }

    public int y() {
        return second;
    }


    public int width() {
        return first;
    }

    public int height() {
        return second;
    }

    @Override
    public String toString() {
        return "IntPair{" +
            "first=" + first +
            ", second=" + second +
            '}';
    }
}
