package io.neovim.java.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author dhleong
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventName {
    String value();
}
