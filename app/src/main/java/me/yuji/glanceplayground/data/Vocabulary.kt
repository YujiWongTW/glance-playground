package me.yuji.glanceplayground.data

import androidx.core.net.toUri

data class Vocabulary(
    val text: String,
    val isLearned: Boolean = false,
) {
    val definitionUrl get() = "https://www.dictionary.com/browse/$text"
}