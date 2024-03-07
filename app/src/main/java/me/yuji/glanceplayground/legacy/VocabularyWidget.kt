package me.yuji.glanceplayground.legacy

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.net.toUri
import me.yuji.glanceplayground.R

class VocabularyWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_SHOW_LEARNED =
            "me.yuji.glanceplayground.legacy.VocabularyWidget.ACTION_TOGGLE_SHOW_LEARNED"
        private const val EXTRA_SHOW_LEARNED = "show_learned"
        private const val ACTION_VIEW_DEFINITION =
            "me.yuji.glanceplayground.legacy.VocabularyWidget.ACTION_VIEW_DEFINITION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        println("onReceive: $intent")
        when (intent.action) {

            ACTION_TOGGLE_SHOW_LEARNED -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                val showLearned = intent.getBooleanExtra(EXTRA_SHOW_LEARNED, false)
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    updateAppWidget(
                        context,
                        AppWidgetManager.getInstance(context),
                        appWidgetId,
                        showLearned
                    )
                }
            }
            
            ACTION_VIEW_DEFINITION -> {
                intent.getStringExtra("url")?.let { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        data = url.toUri()
                    })
                }
            }

            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                true
            )
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        showLearned: Boolean
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_vocabulary)
        views.setCompoundButtonChecked(R.id.show_learned_cb, showLearned)
        views.setOnClickPendingIntent(R.id.show_learned_cb, PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, VocabularyWidget::class.java)
                .apply {
                    setAction(ACTION_TOGGLE_SHOW_LEARNED)
                    putExtra(EXTRA_SHOW_LEARNED, !showLearned)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ))
        views.setPendingIntentTemplate(
            R.id.vocabulary_lv,
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, VocabularyWidget::class.java).apply {
                    setAction(ACTION_VIEW_DEFINITION)
                },
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        views.setRemoteAdapter(
            R.id.vocabulary_lv,
            VocabularyListWidgetService.getIntent(
                context,
                showLearned
            ).apply {
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
        )

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.vocabulary_lv)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
