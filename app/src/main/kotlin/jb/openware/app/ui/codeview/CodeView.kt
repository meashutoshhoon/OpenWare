package jb.openware.app.ui.codeview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import jb.openware.app.R
import java.util.regex.Pattern

@SuppressLint("SetJavaScriptEnabled")
class CodeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var code: String = ""
        private set

    private var escapedCode: String = ""

    var theme: Theme? = null
    var language: Language? = null

    var fontSize: Float = 16f
        set(value) {
            field = value.coerceAtLeast(8f)
            onHighlightListener?.onFontSizeChanged(field.toInt())
        }

    var wrapLine: Boolean = false
    var zoomEnabled: Boolean = false
    var showLineNumber: Boolean = false
    var startLineNumber: Int = 1
        set(value) {
            field = value.coerceAtLeast(1)
        }

    private var lineCount = 0
    private var highlightLineNumber = -1

    private var onHighlightListener: OnHighlightListener? = null
    private val pinchDetector = ScaleGestureDetector(context, PinchListener())

    init {
        initView(attrs)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (zoomEnabled) pinchDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun initView(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.CodeView).apply {
            wrapLine = getBoolean(R.styleable.CodeView_cv_wrap_line, false)
            fontSize = getInt(R.styleable.CodeView_cv_font_size, 14).toFloat()
            zoomEnabled = getBoolean(R.styleable.CodeView_cv_zoom_enable, false)
            showLineNumber = getBoolean(R.styleable.CodeView_cv_show_line_number, false)
            startLineNumber = getInt(R.styleable.CodeView_cv_start_line_number, 1)
            highlightLineNumber = getInt(R.styleable.CodeView_cv_highlight_line_number, -1)
            recycle()
        }

        webChromeClient = WebChromeClient()

        settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            loadWithOverviewMode = true
        }

        setWebContentsDebuggingEnabled(true)
    }

    fun setCode(code: String?): CodeView {
        this.code = code.orEmpty()
        this.escapedCode = Html.escapeHtml(this.code)
        return this
    }

    fun apply() {
        loadDataWithBaseURL(
            "", toHtml(), "text/html", "UTF-8", null
        )
    }

    fun toggleLineNumber() {
        showLineNumber = !showLineNumber
        showHideLineNumber(showLineNumber)
    }

    fun setOnHighlightListener(listener: OnHighlightListener?) {
        onHighlightListener = listener

        if (listener == null) {
            removeJavascriptInterface("android")
            return
        }

        addJavascriptInterface(object {

            @JavascriptInterface
            fun onStartCodeHighlight() {
                listener.onStartCodeHighlight()
            }

            @JavascriptInterface
            fun onFinishCodeHighlight() {
                Handler(Looper.getMainLooper()).post {
                    fillLineNumbers()
                    showHideLineNumber(showLineNumber)
                    highlightLine(highlightLineNumber)
                    listener.onFinishCodeHighlight()
                }
            }

            @JavascriptInterface
            fun onLanguageDetected(name: String, relevance: Int) {
                listener.onLanguageDetected(
                    Language.getLanguageByName(name), relevance
                )
            }

            @JavascriptInterface
            fun onLineClicked(lineNumber: Int, content: String) {
                listener.onLineClicked(lineNumber, content)
            }

        }, "android")
    }

    private fun toHtml(): String = buildString {
        append(
            """
            <!DOCTYPE html>
            <html>
            <head>
                <link rel="stylesheet" href="${theme?.path}" />
                <style>
                    body {
                        font-size:${fontSize.toInt()}px;
                        margin:0;
                        line-height:1.2;
                    }
                    pre { margin:0; position:relative; }
                    table, td, tr { margin:0; padding:0; }
                    td.ln { text-align:right; padding-right:2px; }
                    td.line:hover { background:#661d76; color:#fff; border-radius:2px; }
                    td.destacado { background:#ffda11; color:#000; border-radius:2px; }
                </style>
                <script src="file:///android_asset/highlightjs/highlight.js"></script>
                <script>hljs.initHighlightingOnLoad();</script>
            </head>
            <body>
                <pre><code class="${language?.languageName}">
                    ${insertLineNumbers(escapedCode)}
                </code></pre>
            </body>
            </html>
            """.trimIndent()
        )
    }

    private fun executeJs(js: String) {
        evaluateJavascript("javascript:$js", null)
    }

    private fun fillLineNumbers() {
        executeJs(
            """
            var x=document.querySelectorAll('td.ln');
            for(var i=0;i<x.length;i++){
                x[i].innerHTML=x[i].getAttribute('line');
            }
            """
        )
    }

    private fun showHideLineNumber(show: Boolean) {
        executeJs(
            """
            var x=document.querySelectorAll('td.ln');
            for(var i=0;i<x.length;i++){
                x[i].style.display=${if (show) "''" else "'none'"};
            }
            """
        )
    }

    fun highlightLine(line: Int) {
        highlightLineNumber = line
        executeJs("var x=document.querySelectorAll('.destacado'); if(x[0]) x[0].classList.remove('destacado');")
        if (line >= 0) {
            executeJs("var x=document.querySelector(\"td.line[line='$line']\"); if(x) x.classList.add('destacado');")
        }
    }

    private fun insertLineNumbers(code: String): String {
        val matcher = Pattern.compile("(.*?)&#10;").matcher(code)
        val sb = StringBuffer()
        var line = startLineNumber
        lineCount = 0

        while (matcher.find()) {
            matcher.appendReplacement(
                sb,
                "<tr><td line='$line' class='hljs-number ln'></td><td line='$line' onclick='android.onLineClicked($line, this.textContent);' class='line'>$1</td></tr>&#10;"
            )
            line++
            lineCount++
        }

        return "<table>$sb</table>"
    }

    interface OnHighlightListener {
        fun onStartCodeHighlight()
        fun onFinishCodeHighlight()
        fun onLanguageDetected(language: Language, relevance: Int)
        fun onFontSizeChanged(sizeInPx: Int)
        fun onLineClicked(lineNumber: Int, content: String)
    }

    private inner class PinchListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var oldSize = fontSize

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            fontSize *= detector.scaleFactor
            fontSize = fontSize.coerceAtLeast(8f)

            if (oldSize.toInt() != fontSize.toInt()) {
                executeJs("document.body.style.fontSize='${fontSize.toInt()}px'")
                onHighlightListener?.onFontSizeChanged(fontSize.toInt())
                oldSize = fontSize
            }
            return true
        }
    }
}
