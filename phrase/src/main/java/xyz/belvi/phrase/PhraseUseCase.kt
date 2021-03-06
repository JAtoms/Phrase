package xyz.belvi.phrase

import android.widget.TextView
import xyz.belvi.phrase.helpers.PhraseTranslateListener
import xyz.belvi.phrase.options.PhraseDetected
import xyz.belvi.phrase.options.PhraseOptions
import xyz.belvi.phrase.options.PhraseTranslation
import xyz.belvi.phrase.translateMedium.TranslationMedium

internal interface PhraseUseCase {
    fun bindTextView(
        textView: TextView,
        options: PhraseOptions? = null,
        phraseTranslateListener: PhraseTranslateListener? = null
    )

    fun detect(text: String, options: PhraseOptions? = null): PhraseDetected?
    fun translate(text: String, options: PhraseOptions? = null): PhraseTranslation
    fun updateOptions(options: PhraseOptions)
    fun setTranslationMediums(translationMedium: List<TranslationMedium>)
}
