package com.plcoding.cleanarchitecturenoteapp.feature_note.presentation.add_edit_note.components.html

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.lang.Integer.min

val TAG = "test parseHtml"
/**
 * The tags to interpret. Add tags here and in [tagToStyle].
 */
private val tags = linkedMapOf(
    "<b>" to "</b>",
    "<i>" to "</i>",
    "<u>" to "</u>"
)

/**
 * The main entry point. Call this on a String and use the result in a Text.
 */
@Composable
fun String.parseHtml(): AnnotatedString {
    val newlineReplace = this.replace("<br>", "\n")
    Log.d(TAG, "newlineReplace: " + newlineReplace)

    return buildAnnotatedString {
        recurse(newlineReplace, this)
    }
}

/**
 * Recurses through the given HTML String to convert it to an AnnotatedString.
 * 
 * @param string the String to examine.
 * @param to the AnnotatedString to append to.
 */
@Composable
private fun recurse(string: String, to: AnnotatedString.Builder) {
    Log.d(TAG, "recurse")
    Log.d(TAG, "string: " + string)
    
    //Find the opening tag that the given String starts with, if any.
    val startTag = tags.keys.find { string.startsWith(it) }
    
    //Find the closing tag that the given String starts with, if any.
    val endTag = tags.values.find { string.startsWith(it) }

    when {
        //If the String starts with a closing tag, then pop the latest-applied
        //SpanStyle and continue recursing.
        tags.any { string.startsWith(it.value) } -> {
            to.pop()
            recurse(string.removeRange(0, endTag!!.length), to)
        }
        //If the String starts with an opening tag, apply the appropriate
        //SpanStyle and continue recursing.
        tags.any { string.startsWith(it.key) } -> {
            to.pushStyle(tagToStyle(startTag!!))
            recurse(string.removeRange(0, startTag.length), to)
        }
        //If the String doesn't start with an opening or closing tag, but does contain either,
        //find the lowest index (that isn't -1/not found) for either an opening or closing tag.
        //Append the text normally up until that lowest index, and then recurse starting from that index.
        tags.any { string.contains(it.key) || string.contains(it.value) } -> {
            val firstStart = tags.keys.map { string.indexOf(it) }.filterNot { it == -1 }.minOrNull() ?: -1
            val firstEnd = tags.values.map { string.indexOf(it) }.filterNot { it == -1 }.minOrNull() ?: -1
            val first = when {
                firstStart == -1 -> firstEnd
                firstEnd == -1 -> firstStart
                else -> min(firstStart, firstEnd)
            }
            Log.d(TAG, "string.substring(0, first): " + string.substring(0, first))
            to.append(string.substring(0, first))
            Log.d(TAG, "string.removeRange(0, first): " + string.removeRange(0, first))
            recurse(string.removeRange(0, first), to)
        }
        //There weren't any supported tags found in the text. Just append it all normally.
        else -> {
            Log.d(TAG, "string: " + string)
            to.append(string)
        }
    }
}

// // convert that HTML into an AnnotatedString
// fun String.parseBold(): AnnotatedString {
//     val parts = this.split("<b>", "</b>")
//     return buildAnnotatedString {
//         var bold = false
//         for (part in parts) {
//             if (bold) {
//                 withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                     append(part)
//                 }
//             } else {
//                 append(part)
//             }
//             bold = !bold
//         }
//     }
// }

/**
 * Get a [SpanStyle] for a given (opening) tag.
 * Add your own tag styling here by adding its opening tag to
 * the when clause and then instantiating the appropriate [SpanStyle].
 * 
 * @return a [SpanStyle] for the given tag.
 */
private fun tagToStyle(tag: String): SpanStyle {
    return when (tag) {
        "<b>" -> {
            SpanStyle(fontWeight = FontWeight.Bold)
        }
        "<i>" -> {
            SpanStyle(fontStyle = FontStyle.Italic)
        }
        "<u>" -> {
            SpanStyle(textDecoration = TextDecoration.Underline)
        }
        //This should only throw if you add a tag to the [tags] Map and forget to add it 
        //to this function.
        else -> throw IllegalArgumentException("Tag $tag is not valid.")
    }
}