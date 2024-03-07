package me.yuji.glanceplayground.legacy

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.yuji.glanceplayground.R
import me.yuji.glanceplayground.data.DummyVocabularyRepository
import me.yuji.glanceplayground.data.Vocabulary
import me.yuji.glanceplayground.data.VocabularyRepository


class VocabularyListWidgetService : RemoteViewsService() {

    companion object {
        private const val EXTRA_SHOW_LEARNED = "show_learned"

        fun getIntent(context: Context, showLearned: Boolean): Intent {
            return Intent(context, VocabularyListWidgetService::class.java).apply {
                putExtra(EXTRA_SHOW_LEARNED, showLearned)
            }
        }
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return VocabularyListFactory(
            this.applicationContext,
            DummyVocabularyRepository(),
            intent.getBooleanExtra(EXTRA_SHOW_LEARNED, true)
        )
    }

    private class VocabularyListFactory(
        private val context: Context,
        private val vocabularyRepository: VocabularyRepository,
        private val showLearned: Boolean
    ) : RemoteViewsFactory {

        private val vocabularyList = mutableListOf<Vocabulary>()
        private val scope = CoroutineScope(Dispatchers.IO)

        override fun onCreate() {

        }

        override fun onDataSetChanged() {
            scope.launch {
                vocabularyList.clear()
                vocabularyList.addAll(
                    vocabularyRepository.getAll().filter { showLearned || !it.isLearned }
                )
            }
        }

        override fun onDestroy() {
            scope.cancel()
        }

        override fun getCount(): Int {
            return vocabularyList.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            return bindItem(vocabularyList[position])
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return vocabularyList[position].text.hashCode().toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        private fun bindItem(vocabulary: Vocabulary): RemoteViews {
            return RemoteViews(context.packageName, R.layout.item_vocabulary).apply {
                setTextViewText(
                    R.id.vocabulary_txt,
                    vocabulary.text.let {
                        when {
                            vocabulary.isLearned -> {
                                // draw a line through for learned
                                SpannableString(it).apply {
                                    setSpan(
                                        StrikethroughSpan(),
                                        0,
                                        it.length,
                                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }

                            else -> it
                        }
                    }
                )
                setOnClickFillInIntent(
                    R.id.item_lo,
                    Intent().apply {
                        putExtra("url", vocabulary.definitionUrl)
                    }
                )
            }

        }
    }
}
