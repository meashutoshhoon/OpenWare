package jb.openware.app.ui.components

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.TextView

object TextFormatter {

    fun format(textView: TextView, input: String) {
        val builder = SpannableStringBuilder(input)

        apply(builder, "*b") { StyleSpan(Typeface.BOLD) }
        apply(builder, "*i") { StyleSpan(Typeface.ITALIC) }
        apply(builder, "*u") { UnderlineSpan() }
        apply(builder, "*s") { StrikethroughSpan() }

        textView.text = builder
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun apply(
        builder: SpannableStringBuilder, tag: String, spanFactory: () -> Any
    ) {
        var text = builder.toString()
        var start = text.indexOf(tag)

        while (start != -1) {
            val end = text.indexOf(tag, start + tag.length)
            if (end == -1) break

            // remove tags
            builder.delete(end, end + tag.length)
            builder.delete(start, start + tag.length)

            // apply span to updated builder
            builder.setSpan(
                spanFactory(), start, end - tag.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            text = builder.toString()
            start = text.indexOf(tag)
        }
    }
}
