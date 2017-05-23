package io.neovim.java.event;

/**
 * @author dhleong
 */
public interface Event<T> {
    String kind();
    T value();
}
