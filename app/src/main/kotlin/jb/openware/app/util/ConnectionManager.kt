package jb.openware.app.util

class ConnectionManager {
    private var _params: HashMap<String, Any> = HashMap()
    private var _headers: HashMap<String, Any> = HashMap()
    private var _requestType = 0

    var params: HashMap<String, Any>
        get() = _params
        set(value) {
            _params = value
        }

    var headers: HashMap<String, Any>
        get() {
            return _headers
        }
        set(value) {
            _headers = value
        }

    var requestType: Int
        get() = _requestType
        set(value) {
            _requestType = value
        }

    fun startRequest(url: String, requestListener: RequestListener) {
        startRequest("GET", url, "New_Tag", requestListener)
    }

    fun startRequest(url: String, requestListener: SingleRequestListener) {
        startRequest("GET", url, "New_Tag", requestListener)
    }

    fun startRequest(method: String, url: String, tag: String, requestListener: RequestListener) {
        RequestNetworkController.instance.execute(this, method, url, tag, requestListener)
    }

    fun startRequest(
        method: String, url: String, tag: String, requestListener: SingleRequestListener
    ) {
        RequestNetworkController.instance.execute(this, method, url, tag, object : RequestListener {
                override fun onResponse(
                    tag: String, response: String, responseHeaders: HashMap<String, Any>
                ) {
                    requestListener.onResponse(tag, response, responseHeaders)
                }

                override fun onErrorResponse(tag: String, message: String) {
                    // Handle error response
                }
            })
    }

    interface RequestListener {
        fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>)

        fun onErrorResponse(tag: String, message: String)
    }

    interface SingleRequestListener {
        fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>)
    }
}