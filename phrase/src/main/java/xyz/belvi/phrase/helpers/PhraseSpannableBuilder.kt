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
    private var source: String,
    private val phraseOptions: PhraseOptions? = null
) :
    SpannableStringBuilder(source),
    PhraseTranslateListener {

    private val options = phraseOptions ?: Phrase.instance().phraseImpl.phraseOptions
    private var showingTranslation = false
    private var phraseTranslation: PhraseTranslation? = null

    init {
        buildSpanForTranslation()
    }

    abstract override fun onPhraseTranslating()

    override fun onPhraseTranslated(phraseTranslation: PhraseTranslation?) {
        if (showingTranslation) {
            buildSpanForTranslation()
        } else {
            buildTranslatedPhraseSpan()
        }
        showingTranslation = !showingTranslation
    }

    fun updateSource(source: String) {
        this.source = source
        showingTranslation = true
        onPhraseTranslated(phraseTranslation)
    }

    private fun buildSpanForTranslation() {
        init()
        requireNotNull(options)
        val behaviors = options.behavioursOptions.behaviours
        if ((behaviors.skipDetection() && !behaviors.ignoreSkipDetection()) || source.isEmpty())
            return
        Phrase.instance().detectLanguage(source).let { phraseDetected ->
            phraseDetected?.let {
                if (behaviors.translatePreferredSourceOnly()) {
                    val sourceIndex =
                        options.sourcePreferredTranslation.sourceTranslateOption.indexOfFirst { it.source == phraseDetected.code }
                    if (sourceIndex < 0)
                        return
                }
                if (phraseDetected.code == options.targetLanguageCode || options.excludeSources.contains(
                        phraseDetected.code
                    )
                ) {
                    return
                }
            }
            if (!behaviors.hideTranslatePrompt()) {
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
        }
    }

    private fun buildTranslatedPhraseSpan() {
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
                        append(" ${phraseTranslation.translationMedium?.name()}")
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
    }

    private fun init() {
        clear()
        append(source)
    }

    inner class SpannablePhraseClickableSpan : ClickableSpan() {
        override fun onClick(widget: View) {
            if (phraseTranslation == null || phraseTranslation?.source?.text != source) {
                onPhraseTranslating()
                phraseTranslation = Phrase.instance().translate(source, phraseOptions)
            }
            onPhraseTranslated(phraseTranslation)
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