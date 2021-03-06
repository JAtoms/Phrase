package xyz.belvi.phrase.options

import androidx.annotation.IntDef
import xyz.belvi.phrase.options.Behaviour.Companion.BEHAVIOR_HIDE_CREDIT_SIGNATURE
import xyz.belvi.phrase.options.Behaviour.Companion.BEHAVIOR_HIDE_TRANSLATE_PROMPT
import xyz.belvi.phrase.options.Behaviour.Companion.BEHAVIOR_IGNORE_DETECTION
import xyz.belvi.phrase.options.Behaviour.Companion.BEHAVIOR_REPLACE_SOURCE_TEXT
import xyz.belvi.phrase.options.Behaviour.Companion.BEHAVIOR_TRANSLATE_SOURCE_OPTION_ONLY

data class PhraseDetected(
    val text: String,
    val languageCode: String,
    val languageName: String,
    val detectionMediumName: String
)

data class PhraseTranslation(
    val translation: String,
    val translationMediumName: String?,
    val detectedSource: PhraseDetected?
)

class Behaviour(private val behaviorSet: Set<@BehaviorFlags Int> = setOf()) {

    companion object {
        const val BEHAVIOR_REPLACE_SOURCE_TEXT: Int = 1

        const val BEHAVIOR_TRANSLATE_SOURCE_OPTION_ONLY: Int = 2

        const val BEHAVIOR_IGNORE_DETECTION: Int = 3

        const val BEHAVIOR_HIDE_CREDIT_SIGNATURE: Int = 4

        const val BEHAVIOR_HIDE_TRANSLATE_PROMPT: Int = 5
    }

    internal fun replaceSourceText() = behaviorSet.contains(BEHAVIOR_REPLACE_SOURCE_TEXT)

    internal fun ignoreDetection() = behaviorSet.contains(BEHAVIOR_IGNORE_DETECTION)

    internal fun translatePreferredSourceOnly() =
        behaviorSet.contains(BEHAVIOR_TRANSLATE_SOURCE_OPTION_ONLY)

    internal fun hideSignature() = behaviorSet.contains(BEHAVIOR_HIDE_CREDIT_SIGNATURE)

    internal fun hideTranslatePrompt() = behaviorSet.contains(BEHAVIOR_HIDE_TRANSLATE_PROMPT)
}

@Target(AnnotationTarget.TYPE)
@IntDef(
    flag = true,
    value = [
        BEHAVIOR_REPLACE_SOURCE_TEXT,
        BEHAVIOR_TRANSLATE_SOURCE_OPTION_ONLY,
        BEHAVIOR_IGNORE_DETECTION,
        BEHAVIOR_HIDE_CREDIT_SIGNATURE,
        BEHAVIOR_HIDE_TRANSLATE_PROMPT
    ]
)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class BehaviorFlags
