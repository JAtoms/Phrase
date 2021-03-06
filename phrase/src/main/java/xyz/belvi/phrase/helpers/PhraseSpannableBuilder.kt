package xyz.belvi.phrase.helpers

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.TypefaceSpan
import android.view.View
import xyz.belvi.phrase.Phrase
import xyz.belvi.phrase.options.PhraseOptions
import xyz.belvi.phrase.options.PhraseTranslation

abstract class PhraseSpannableBuilder constructor(
    protected var source: String,
    protected var phraseOptions: PhraseOptions? = null
) :
    SpannableStringBuilder(source),
    PhraseTranslateListener {

    protected var showingTranslateAction = false
    protected var phraseTranslation: PhraseTranslation? = null

    init {
        buildTranslateActionSpan()
    }

    fun updateOptions(options: PhraseOptions) {
        this.phraseOptions = options
        buildTranslateActionSpan()
        onContentChanged(this)
    }

    fun updateSource(source: String) {
        this.source = source
        buildTranslateActionSpan()
        onContentChanged(this)
    }

    private fun options() = phraseOptions ?: Phrase.instance().phraseImpl.phraseOptions

    private fun buildTranslateActionSpan() {
        init()
        val options = options()
        requireNotNull(options)
        val behaviors = options.behavioursOptions.behaviours
        val detectedMedium =
            if (behaviors.ignoreDetection() || source.isEmpty())
                null
            else Phrase.instance().detectLanguage(source)

        detectedMedium?.let { phraseDetected ->
            if (behaviors.translatePreferredSourceOnly()) {
                val allowTranslation =
                    options.sourcePreferredTranslation.sourceTranslateOption.filter { it.sourceLanguageCode != phraseDetected.languageCode }
                        .let { sourceOptions ->
                            sourceOptions.find {
                                it.targetLanguageCode.contains(options.targetLanguageCode) || it.targetLanguageCode.contains(
                                    "*"
                                )
                            }?.let { true } ?: false
                        }
                if (!allowTranslation)
                    return
            }
            if (phraseDetected.languageCode == options.targetLanguageCode || options.excludeSources.contains(
                    phraseDetected.languageCode
                )
            ) {
                return
            }
        }

        if (!source.isNullOrBlank() && !behaviors.hideTranslatePrompt()) {
            appendln("\n")
            val start = length
            append(options.translateText)
            setSpan(
                SpannablePhraseClickableSpan(),
                start,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        showingTranslateAction = true
    }

    private fun buildTranslatedPhraseSpan() {
        val options = options()
        requireNotNull(options)
        val optionBehavior = options.behavioursOptions.behaviours
        phraseTranslation?.let { phraseTranslation ->
            init()
            appendln("\n")
            if (optionBehavior.replaceSourceText()) {
                clear()
            }
            if (!optionBehavior.hideTranslatePrompt()) {
                var start = length
                append(options.translateFrom.invoke(phraseTranslation))
                setSpan(
                    SpannablePhraseClickableSpan(),
                    start,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                if (!optionBehavior.hideSignature()) {
                    options.behavioursOptions.signatureTypeFace?.let { typeFace ->
                        start = length
                        append("${phraseTranslation.translationMediumName}")
                        setSpan(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) TypefaceSpan(
                                typeFace
                            ) else CustomTypefaceSpan(
                                typeFace
                            ),
                            start,
                            length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        options.behavioursOptions.signatureColor.let { color ->
                            setSpan(
                                ForegroundColorSpan(color),
                                start,
                                length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
                appendln("\n")
            }
            append(phraseTranslation.translation)
        }
        showingTranslateAction = false
    }

    private fun init() {
        clear()
        append(source)
    }

    inner class SpannablePhraseClickableSpan : ClickableSpan() {
        override fun onClick(widget: View) {
            onActionClick(showingTranslateAction)
            if (showingTranslateAction) {
                onPhraseTranslating()
                val options = options()
                phraseTranslation = Phrase.instance().translate(source, options)
                buildTranslatedPhraseSpan()
                onPhraseTranslated(phraseTranslation)
            } else {
                buildTranslateActionSpan()
            }
            onContentChanged(this@PhraseSpannableBuilder)
            widget.invalidate()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }

    internal class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
        override fun updateDrawState(ds: TextPaint) {
            applyCustomTypeFace(ds, typeface)
        }

        override fun updateMeasureState(paint: TextPaint) {
            applyCustomTypeFace(paint, typeface)
        }

        companion object {
            private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
                paint.typeface = tf
            }
        }
    }
}
