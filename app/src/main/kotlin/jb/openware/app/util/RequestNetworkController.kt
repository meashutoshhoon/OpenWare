package jb.openware.app.util

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.collections.iterator

class RequestNetworkController private constructor() {
    companion object {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DELETE = "DELETE"

        const val REQUEST_PARAM = 0
        const val REQUEST_BODY = 1

        private const val SOCKET_TIMEOUT = 15000
        private const val READ_TIMEOUT = 25000
        private var mInstance: RequestNetworkController? = null

        @get:Synchronized
        val instance: RequestNetworkController
            get() {
                if (mInstance == null) {
                    mInstance = RequestNetworkController()
                }
                return mInstance!!
            }
    }

    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private var client: OkHttpClient? = null

    private fun getClient(): OkHttpClient {
        if (client == null) {
            val builder = OkHttpClient.Builder()

            try {
                val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                })

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, SecureRandom())
                val sslSocketFactory = sslContext.socketFactory
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.connectTimeout(SOCKET_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                builder.readTimeout(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                builder.writeTimeout(READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                builder.hostnameVerifier { _, _ -> true }
            } catch (_: Exception) {
            }

            client = builder.build()
        }

        return client!!
    }

    fun execute(
        connectionManager: ConnectionManager,
        method: String,
        url: String,
        tag: String,
        requestListener: ConnectionManager.RequestListener
    ) {
        val reqBuilder = Request.Builder()
        val headerBuilder = Headers.Builder()

        if (connectionManager.headers.isNotEmpty()) {
            val headers = connectionManager.headers

            for ((key, value) in headers) {
                headerBuilder.add(key, value.toString())
            }
        }

        try {
            if (connectionManager.requestType == REQUEST_PARAM) {
                if (method == GET) {
                    val httpBuilder = url.toHttpUrlOrNull()?.newBuilder()
                        ?: throw NullPointerException("unexpected url: $url")

                    if (connectionManager.params.isNotEmpty()) {
                        val params = connectionManager.params

                        for ((key, value) in params) {
                            httpBuilder.addQueryParameter(key, value.toString())
                        }
                    }

                    reqBuilder.url(httpBuilder.build()).headers(headerBuilder.build()).get()
                } else {
                    val formBuilder = FormBody.Builder()
                    if (connectionManager.params.isNotEmpty()) {
                        val params = connectionManager.params

                        for ((key, value) in params) {
                            formBuilder.add(key, value.toString())
                        }
                    }

                    val reqBody = formBuilder.build()

                    reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody)
                }
            } else {
                val reqBody = Gson().toJson(connectionManager.params)
                    .toRequestBody("application/json".toMediaTypeOrNull())

                if (method == GET) {
                    reqBuilder.url(url).headers(headerBuilder.build()).get()
                } else {
                    reqBuilder.url(url).headers(headerBuilder.build()).method(method, reqBody)
                }
            }

            val req = reqBuilder.build()

            getClient().newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    mainHandler.post { requestListener.onErrorResponse(tag, e.message ?: "") }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()?.trim() ?: ""
                    mainHandler.post {
                        val responseHeaders = HashMap<String, Any>()
                        val headers = response.headers

                        for (i in 0 until headers.size) {
                            responseHeaders[headers.name(i)] = headers.value(i)
                        }

                        requestListener.onResponse(tag, responseBody, responseHeaders)
                    }
                }
            })
        } catch (e: Exception) {
            requestListener.onErrorResponse(tag, e.message ?: "")
        }
    }
}
