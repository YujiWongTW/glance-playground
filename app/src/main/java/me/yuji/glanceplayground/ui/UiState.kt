package me.yuji.glanceplayground.ui

import me.yuji.glanceplayground.data.Vocabulary

internal data class UiState(
    val vocabularyList: List<Vocabulary>,
    val showLearned: Boolean = false
)