package org.neojet.util

import io.neovim.java.IntPair

/**
 * @author dhleong
 */

operator fun IntPair.component1(): Int = first
operator fun IntPair.component2(): Int = second
