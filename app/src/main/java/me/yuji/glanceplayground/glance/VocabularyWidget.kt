package me.yuji.glanceplayground.glance

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentSize
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.yuji.glanceplayground.R
import me.yuji.glanceplayground.data.DummyVocabularyRepository
import me.yuji.glanceplayground.data.Vocabulary
import me.yuji.glanceplayground.data.VocabularyRepository
import me.yuji.glanceplayground.ui.UiState

class VocabularyWidget(
    private val vocabularyRepository: VocabularyRepository = DummyVocabularyRepository()
) : GlanceAppWidget() {

    companion object {
        private val PREF_KEY_SHOW_LEARNED = booleanPreferencesKey("show_learned")
        private const val DEFAULT_SHOW_LEARNED = true
    }

    private val vocabularyListStateFlow = MutableStateFlow(emptyList<Vocabulary>())
    private val showLearnedStateFlow = MutableStateFlow(DEFAULT_SHOW_LEARNED)
    private val uiStateFlow = combine(
        vocabularyListStateFlow,
        showLearnedStateFlow
    ) { list, showLearned ->
        UiState(list.filter { showLearned || !it.isLearned }, showLearned)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {

            val state: Preferences = currentState()

            GlanceTheme {
                val uiState by uiStateFlow.collectAsState(
                    initial = UiState(emptyList(), state[PREF_KEY_SHOW_LEARNED] ?: DEFAULT_SHOW_LEARNED)
                )

                LaunchedEffect(Unit) {
                    showLearnedStateFlow.value = state[PREF_KEY_SHOW_LEARNED] ?: DEFAULT_SHOW_LEARNED
                    val vocabularyList = vocabularyRepository.getAll()
                    vocabularyListStateFlow.value = vocabularyList
                }

                val coroutineScope = rememberCoroutineScope()
                VocabularyWidgetContent(
                    uiState = uiState,
                    onToggleShowLearned = {
                        showLearnedStateFlow.value = !showLearnedStateFlow.value
                        coroutineScope.launch {
                            updateAppWidgetState(context, id) { pref ->
                                pref[PREF_KEY_SHOW_LEARNED] = showLearnedStateFlow.value
                            }
                        }
                    },
                    onClick = { vocabulary -> navToDefinition(context, vocabulary) }
                )
            }
        }
    }

    private fun navToDefinition(context: Context, vocabulary: Vocabulary) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = vocabulary.definitionUrl.toUri()
            }
        )
    }
}

@Composable
private fun VocabularyWidgetContent(
    uiState: UiState,
    onToggleShowLearned: () -> Unit,
    onClick: (Vocabulary) -> Unit
) {

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(imageProvider = ImageProvider(R.drawable.app_widget_background))
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {

        Row(
            modifier = GlanceModifier
                .wrapContentSize()
                .padding(4.dp)
        ) {
            CheckBox(
                checked = uiState.showLearned,
                text = "Show learned",
                onCheckedChange = onToggleShowLearned
            )
        }

        VocabularyList(vocabularyList = uiState.vocabularyList, onClick = onClick)
    }
}

@Composable
private fun VocabularyList(vocabularyList: List<Vocabulary>, onClick: (Vocabulary) -> Unit) {
    LazyColumn {
        items(vocabularyList, itemId = { it.text.hashCode().toLong() }) { vocabulary ->
            VocabularyItem(
                vocabulary,
                modifier = GlanceModifier.clickable { onClick(vocabulary) }
            )
        }
    }
}

@Composable
private fun VocabularyItem(vocabulary: Vocabulary, modifier: GlanceModifier = GlanceModifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = vocabulary.text,
            style = TextDefaults.defaultTextStyle.copy(
                fontSize = 14.sp,
                textDecoration = if (vocabulary.isLearned) TextDecoration.LineThrough else null
            )
        )
    }
}
