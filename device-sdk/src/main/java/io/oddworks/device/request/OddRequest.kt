package io.oddworks.device.request

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.Nullable
import android.util.Log
import android.widget.Toast
import com.squareup.okhttp.*
import io.oddworks.device.Oddworks
import io.oddworks.device.R
import io.oddworks.device.authentication.OddAuthenticator
import io.oddworks.device.exception.BadResponseCodeException
import io.oddworks.device.exception.OddParseException
import io.oddworks.device.exception.OddRequestException
import io.oddworks.device.metric.OddMetric
import io.oddworks.device.model.OddAuthentication
import io.oddworks.device.model.OddProgress
import io.oddworks.device.model.OddViewer
import io.oddworks.device.model.OddWatchlist
import io.oddworks.device.model.common.OddResource
import io.oddworks.device.model.common.OddResourceType
import org.json.JSONException
import rx.Observable
import java.io.IOException
import java.util.*

class OddRequest(
        private val context: Context,
        private val resourceType: OddResourceType,
        private val resourceId: String? = null,
        private val apiBaseURL: String? = null,
        private val account: Account? = null,
        private val authorizationJWT: String? = null,
        private val acceptLanguageHeader: String?,
        private val versionName: String? = null,
        private val relationshipName: String?,
        private val include: String? = null,
        private val limit: Int? = null,
        private val offset: Int? = null,
        private val sort: String? = null,
        private val query: String? = null,
        private val event: OddMetric? = null,
        private val authentication: OddAuthentication? = null,
        private val watchlist: OddWatchlist? = null,
        private val progress: OddProgress? = null,
        private val skipCache: Boolean = false) {

    private val accountManager by lazy {
        context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    }

    private val packageManager by lazy {
        context.packageManager
    }

    private val applicationInfo by lazy {
        packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    }

    private val packageInfo by lazy {
        packageManager.getPackageInfo(context.packageName, 0)
    }

    private val metaData: Bundle? by lazy {
        applicationInfo.metaData
    }


    private val baseURL: HttpUrl
        get() {
            val baseUrlString = when {
                isEventResourceType() -> {
                    apiBaseURL ?: metaData?.getString(Oddworks.ANALYTICS_API_BASE_URL_KEY, Oddworks.DEFAULT_ANALYTICS_API_BASE_URL) ?: throw OddRequestException("Missing ${Oddworks.ANALYTICS_API_BASE_URL_KEY} in Application meta-data")
                }
                else -> {
                    apiBaseURL ?: metaData?.getString(Oddworks.API_BASE_URL_KEY, Oddworks.DEFAULT_API_BASE_URL) ?: throw OddRequestException("Missing ${Oddworks.API_BASE_URL_KEY} in Application meta-data")
                }
            }
            return HttpUrl.parse(baseUrlString)
        }

    private val oddUserAgent: String
        get() {
            val version = versionName ?: packageInfo.versionName ?: throw OddRequestException("Application PackageInfo versionName is somehow missing")
            return "model[manufacturer]=${Build.MANUFACTURER}&model[brand]=${Build.BRAND}&model[name]=${Build.MODEL}&model[version]=${Build.PRODUCT}&os[name]=${Build.VERSION.RELEASE}&os[version]=${Build.VERSION.SDK_INT}&build[version]=$version"
        }

    private val acceptLanguage: String
        get() {
            return acceptLanguageHeader ?: LOCALE
        }

    private val viewerJWT: String?
        get() {
            if (account == null) {
                return null
            } else {
                try {
                    return accountManager.blockingGetAuthToken(account, OddAuthenticator.AUTH_TOKEN_TYPE_ODDWORKS_DEVICE, true)
                } catch (e: Exception) {
                    Log.w(TAG, "viewerJWT failed - ${e.message}")
                }
                return null
            }
        }

    private val authorization: String
        get() {
            val jwt = authorizationJWT ?: viewerJWT ?: metaData?.getString(Oddworks.CONFIG_JWT_KEY) ?: throw OddRequestException("Missing ${Oddworks.CONFIG_JWT_KEY} in Application meta-data")
            return "Bearer $jwt"
        }

    constructor(builder: Builder): this(builder.context,
            builder.resourceType,
            builder.resourceId,
            builder.apiBaseURL,
            builder.account,
            builder.authorizationJWT,
            builder.acceptLanguageHeader,
            builder.versionName,
            builder.relationshipName,
            builder.include,
            builder.limit,
            builder.offset,
            builder.sort,
            builder.query,
            builder.event,
            builder.authentication,
            builder.watchlist,
            builder.progress,
            builder.skipCache) {
    }

    init {
        // Set OkHttp Cache if it isn't already set
        if (OKHTTP_CLIENT.cache == null) {
            OKHTTP_CLIENT.cache = Cache(context.cacheDir, MAX_CACHE_SIZE)
        }

        if (null == event && resourceType == OddResourceType.EVENT) {
            throw OddRequestException("Missing event metric for POST request")
        }
    }

    /**
     * Builds and executes a [Request]
     *
     * @param oddCallback - an [OddCallback]
     */
    fun <T> enqueueRequest(oddCallback: OddCallback<T>) {
        // get base endpoint
        val endpoint = baseURL.newBuilder()
        // add uri
        getPath().split("/").forEach {
            endpoint.addPathSegment(it)
        }
        getQueryParameters().forEach {
            endpoint.addQueryParameter(it.key, it.value)
        }
        // add headers
        val builder = Request.Builder()
                .url(endpoint.toString())
                .addHeader("authorization", authorization)
                .addHeader("x-odd-user-agent", oddUserAgent)
                .addHeader("accept", ACCEPT_HEADER)
                .addHeader("accept-language", acceptLanguage)


        val request = when {
            isEventResourceType() -> {
                builder
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .post(RequestBody.create(JSON, event!!.toJSONObject().toString()))
                        .build()
            }
            isLogin() -> {
                builder
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .post(RequestBody.create(JSON, authentication!!.toJSONObject().toString()))
                        .build()
            }
            isWatchlistRemove() -> {
                builder
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .delete(RequestBody.create(JSON, watchlist!!.toJSONObject().toString()))
                        .build()
            }
            isWatchlistAdd() -> {
                builder
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .post(RequestBody.create(JSON, watchlist!!.toJSONObject().toString()))
                        .build()
            }
            isVideoProgress() -> {
                builder
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .post(RequestBody.create(JSON, progress!!.toJSONObject().toString()))
                        .build()
            }
            shouldSkipCache() -> {
                builder
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build()
            }
            else -> {
                builder.build()
            }
        }
        Log.d(OddRequest::class.java.simpleName, request.toString())

        val callback = getCallback(oddCallback, getParseCall<T>())

        OKHTTP_CLIENT.newCall(request).enqueue(callback)
    }

    private fun <T> getCallback(oddCallback: OddCallback<T>, parseCall: ParseCall<T>): Callback {
        return object : Callback {
            override fun onFailure(request: Request, e: IOException) {
                Log.e("ResponseCb onFailure", "Failed", e)
                oddCallback.onFailure(e)
            }

            override fun onResponse(response: Response) {
                Log.d("ResponseCb onResponse", "code: ${response.code()} responseBody: ${response.body()}")

                val observable = when {
                    response.isSuccessful && (response.code() == 202 || response.code() == 204) -> {
                        Observable.create<T> {
                            try {
                                val obj = parseCall.parse("")
                                it.onNext(obj)
                            } catch (e: Exception) {
                                it.onError(OddParseException("Response body parse failed", e))
                            } finally {
                                it.onCompleted()
                            }
                        }
                    }
                    response.isSuccessful -> {
                        Observable.create<T> {
                            var responseBody = ""
                            try {
                                responseBody = response.body().string()
                                val obj = parseCall.parse(responseBody)
                                it.onNext(obj)
                            } catch (e: Exception) {
                                if (responseBody.isNullOrBlank()) {
                                    responseBody = "response.body() was empty"
                                }
                                it.onError(OddParseException("Response body parse failed: $responseBody", e))
                            } finally {
                                response.body().close()
                                it.onCompleted()
                            }
                        }
                    }
                    else -> {
                        if (account != null && response.code() == 401) {
                            // remove account and clear cache
                            if (Build.VERSION.SDK_INT < 22) {
                                accountManager.removeAccount(account, null, null)
                            } else {
                                accountManager.removeAccountExplicitly(account)
                            }

                            // Notify user that login failed
                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                Toast.makeText(context, context.getString(R.string.oddworks_authentication_failed_with_detail, context.getText(R.string.oddworks_account_logged_out)), Toast.LENGTH_SHORT).show()
                            }

                            OKHTTP_CLIENT.cache = Cache(context.cacheDir, MAX_CACHE_SIZE)
                        }

                        // try to get OddErrors, if any
                        try {
                            val oddErrors = OddParser.parseErrorMessage(response.body().string())

                            Observable.error<T>(BadResponseCodeException(response.code(), oddErrors))
                        } catch (e: Exception) {
                            Observable.error<T>(BadResponseCodeException(response.code()))
                        }
                    }
                }
                observable.subscribe({
                    oddCallback.onSuccess(it)
                }, {
                    oddCallback.onFailure(it)
                })
            }
        }
    }



    private interface ParseCall<out Any> {
        @Throws(JSONException::class)
        fun parse(responseBody: String): Any
    }

    @Throws(JSONException::class)
    private fun <T> getParseCall(): ParseCall<T> {
        return object : ParseCall<T> {
            override fun parse(responseBody: String): T {
                @Suppress("UNCHECKED_CAST")
                return when {
                    isEventResourceType() -> {
                        event as T
                    }
                    isWatchlistAdd() || isWatchlistRemove() -> {
                        watchlist!!.resource as T
                    }
                    isVideoProgress() -> {
                        progress as T
                    }
                    isListEndpoint() -> {
                        OddParser.parseMultipleResponse(responseBody) as T
                    }
                    else -> {
                        OddParser.parseSingleResponse(responseBody) as T
                    }
                }
            }
        }
    }

    private fun getPath(): String {
        return when {
            isLogin() -> {
                // /login
                OddAuthentication.ENDPOINT
            }
            isWatchlistAdd() || isWatchlistRemove() -> {
                // /viewers/viewerId/relationships/watchlist
                OddResourceType.WATCHLIST.endpoint.replace(":id", watchlist!!.viewerId)
            }
            isVideoProgress() -> {
                // /videos/:id/progress
                OddResourceType.PROGRESS.endpoint.replace(":id", progress!!.videoId)
            }
            resourceId == null && resourceType != OddResourceType.VIEWER -> {
                // /resourceType
                resourceType.endpoint
            }
            relationshipName == null -> {
                // /resourceType/resourceId
                "${resourceType.endpoint}/$resourceId"
            }
            include == null -> {
                // /resourceType/resourceId/relationships/{relationshipName}
                "${resourceType.endpoint}/$resourceId/relationships/$relationshipName"
            }
            else -> {
                // this should throw
                throw OddRequestException("Unable to determine request path")
            }
        }
    }

    private fun getQueryParameters(): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        if (relationshipName == null && include != null && !isListEndpoint() && !isEventResourceType() && resourceType != OddResourceType.CONFIG) {
            parameters.put("include", include)
        }
        if (limit != null && isListEndpoint()) {
            parameters.put("limit", limit.toString())
        }
        if (offset != null && isListEndpoint()) {
            parameters.put("offset", offset.toString())
        }
        if (sort != null && isListEndpoint()) {
            parameters.put("sort", sort)
        }
        if (query != null && resourceType == OddResourceType.SEARCH) {
            parameters.put("q", query)
        }
        return parameters
    }

    private fun isListEndpoint(): Boolean {
        return (resourceType != OddResourceType.CONFIG && resourceType != OddResourceType.EVENT)
                && ((resourceId == null && relationshipName == null) || (resourceId != null && relationshipName != null))
                && (!isLogin())
    }

    private fun isEventResourceType(): Boolean {
        return resourceType == OddResourceType.EVENT
    }

    private fun isLogin(): Boolean {
        return resourceType == OddResourceType.VIEWER && authentication != null
    }

    private fun isWatchlistAdd(): Boolean {
        return resourceType == OddResourceType.WATCHLIST && watchlist != null && watchlist.addToWatchlist
    }

    private fun isWatchlistRemove(): Boolean {
        return resourceType == OddResourceType.WATCHLIST && watchlist != null && !watchlist.addToWatchlist
    }

    private fun isVideoProgress(): Boolean {
        return resourceType == OddResourceType.PROGRESS && progress != null
    }

    private fun shouldSkipCache(): Boolean {
        if (skipCache) return true
        return when (resourceType) {
            OddResourceType.COLLECTION -> { skipCache }
            OddResourceType.PROMOTION -> { skipCache }
            OddResourceType.VIDEO -> { skipCache }
            OddResourceType.VIEW -> { skipCache }
            else -> { true }
        }
    }

    /**
     * Build an OddRequest
     *
     * @param context - provide context for fetching package meta data
     * @param resourceType - provide [OddResourceType] to specify REST resource
     */
    class Builder(val context: Context, val resourceType: OddResourceType) {
        var account: Account? = null
        var authentication: OddAuthentication? = null
        var include: String? = null
        var authorizationJWT: String? = null
        var versionName: String? = null
        var acceptLanguageHeader: String? = null
        var apiBaseURL: String? = null
        var resourceId: String? = null
        var relationshipName: String? = null
        var limit: Int? = null
        var offset: Int? = null
        var sort: String? = null
        var query: String? = null
        var event: OddMetric? = null
        var watchlist: OddWatchlist? = null
        var progress: OddProgress? = null
        var skipCache: Boolean = false

        /**
         * Allows Builder to tell which resource id to request
         *
         * Requires [resourceType] to be present
         *
         * Ex. `resourceId("12345")`
         * `https://base.url/resourceType/12345`
         *
         * @param resourceId - the OddResource ID
         */
        fun resourceId(resourceId: String): Builder {
            this.resourceId = resourceId
            return this
        }

        /**
         * Allows Builder to tell when to request a resource's relationship resources
         *
         * Requires [resourceType] and [resourceId] to be present.
         *
         * Ex. `relationshipName("videos")`
         * `https://base.url/resourceType/resourceId/relationships/videos`
         *
         * @param relationshipName - the relationship name
         */
        fun relationshipName(relationshipName: String): Builder {
            this.relationshipName = relationshipName
            return this
        }

        /**
         * Used to specify which relationships' resources you want to include in the
         * response. The relationship must exist.
         *
         * For best results, use sparingly. This can drastically increase response sizes
         * if a relationship's data set is large.
         *
         * When present will enable 'include' query parameter.
         *
         * Ex: `include("relationship,relationship2")`
         * `https://base.url/resourceType/resourceId?include=relationship,relationship2`
         *
         * This is only applied to single-resource response types (non-list)
         *
         * @param include - comma separated list of relationship names to include
         * with request
         */
        fun include(include: String): Builder {
            this.include = include
            return this
        }

        /**
         * Used to specify the OddViewer Account which contains an Authorization JWT.
         *
         * This can be overridden by [authorizationJWT]
         *
         * @param account - the android Account to use for authorizing the request.
         */
        fun account(@Nullable account: Account?): Builder {
            this.account = account
            return this
        }

        /**
         * Overrides the default Authorization JWT. This should be overridden for all
         * non-config requests.
         *
         * Defaults to the token specified in [Oddworks.CONFIG_JWT_KEY] in the application
         * meta data.
         *
         * @param authorizationJWT - the JWT you wish to specify
         */
        fun authorizationJWT(authorizationJWT: String): Builder {
            this.authorizationJWT = authorizationJWT
            return this
        }

        /**
         * Overrides the default versionName of the application, taken from the
         * applications package info.
         *
         * This is only used in the `x-odd-user-agent` header used by metric events service.
         *
         * @param versionName - the version name you wish to specify
         */
        fun versionName(versionName: String): Builder {
            this.versionName = versionName
            return this
        }

        /**
         * Overrides the default base URL of the Oddworks instance where requests
         * will be sent.
         *
         * The default is set in [Oddworks.API_BASE_URL_KEY] in the
         * application meta data.
         *
         * If not set there, a fallback of [Oddworks.DEFAULT_API_BASE_URL]
         * is used.
         *
         * This is usually not necessary to override.
         *
         * @param apiBaseURL - the base URL for your Oddworks instance
         */
        fun apiBaseURL(apiBaseURL: String): Builder {
            this.apiBaseURL = apiBaseURL
            return this
        }

        /**
         * Overrides the default Accept-Language header.
         *
         * Defaults to a combination of [Locale] default language and country.
         *
         * @param acceptLanguage - the accept language header value
         */
        fun acceptLanguage(acceptLanguage: String): Builder {
            this.acceptLanguageHeader = acceptLanguage
            return this
        }

        /**
         * Limits the number of results from a multi-resource (list) request.
         *
         * Ex. `limit(5)`
         * `https://base.url/resourceType?page\[limit\]=5`
         *
         * @param limit - the number of resources to return
         */
        fun limit(limit: Int): Builder {
            this.limit = limit
            return this
        }

        /**
         * Offset the result set in a multi-resource (list) request.
         *
         * Ex. `offset(10)`
         * `https://base.url/resourceType?page\[offset\]=10`
         *
         * @param offset - the number of resources to offset
         */
        fun offset(offset: Int): Builder {
            this.offset = offset
            return this
        }

        /**
         * Sort the result set in a multi-resource (list) request.
         *
         * Use this to specify the property used to sort the result
         * set with a single property using dot notation.
         *
         * Ex. `sort("meta.source")`
         * `https://base.url/resourceType?sort=meta.source`
         *
         * @param sort - the property to use to sort the result set
         */
        fun sort(sort: String): Builder {
            this.sort = sort
            return this
        }

        /**
         * Specifies the search query.
         *
         * <p>Required when [resourceType] is [OddResourceType.SEARCH]
         *
         * Ex. `query("the term")`
         * `https://base.url/search?q=the%20term`
         *
         * @param query - the search term
         */
        fun query(query: String): Builder {
            this.query = query
            return this
        }

        /**
         * Specifies the [OddMetric] event to POST.
         *
         * Required when [resourceType] is [OddResourceType.EVENT]
         *
         * @param event - the event metric
         */
        fun event(event: OddMetric): Builder {
            this.event = event
            return this
        }

        /**
         * Specifies the [OddAuthentication] resource to POST.
         *
         * Required when [resourceType] is [OddResourceType.VIEWER]
         *
         * @param email - the email address
         * @param password - the password
         */
        fun login(email: String, password: String): Builder {
            val authentication = OddAuthentication(email, password)
            this.authentication = authentication
            return this
        }

        /**
         * Specifies the [OddResource] to POST to the given [OddViewer]'s watchlist
         * relationship.
         *
         * @param viewerId - specifies the id of the OddViewer
         * @param resource - specifies the resource to add to the watchlist
         */
        fun addResourceToWatchlist(viewerId: String, resource: OddResource): Builder {
            this.watchlist = OddWatchlist(viewerId, resource, true)
            return this
        }

        /**
         * Specifies the [OddResource] to DELETE from the given [OddViewer]'s
         * watchlist relationship.
         *
         * @param viewerId - specifies the id of the OddViewer
         * @param resource - specifies the resource to remove from the watchlist
         */
        fun removeResourceFromWatchlist(viewerId: String, resource: OddResource): Builder {
            this.watchlist = OddWatchlist(viewerId, resource, false)
            return this
        }

        /**
         * Records the [OddProgress] which updates the [OddViewer]'s progress for a given [OddVideo]
         *
         * @param videoId - the id of the OddVideo
         * @param position - the position within the video
         * @param complete - whether or not the viewer has completed the video
         */
        fun recordProgress(videoId: String, position: Int, complete: Boolean = false): Builder {
            this.progress = OddProgress(videoId, position, complete)
            return this
        }

        /**
         * Force OkHttp to hit Oddworks API instead of relying on cache.
         *
         * Defaults to `true`
         *
         * Certain endpoints are not cacheable. Also, see [OddRequest.MAX_CACHE_SIZE]
         *
         * @param skipCache - whether to skip cache and hit API
         */
        fun skipCache(skipCache: Boolean): Builder {
            this.skipCache = skipCache
            return this
        }

        fun build(): OddRequest {
            return OddRequest(this)
        }
    }


    companion object {
        private val TAG = OddRequest::class.java.simpleName
        private val JSON = MediaType.parse("application/json; charset=utf-8")
        private val ACCEPT_HEADER = "application/json"
        private val LANGUAGE = Locale.getDefault().language.toLowerCase()
        private val COUNTRY = Locale.getDefault().country.toLowerCase()
        private val LOCALE = "$LANGUAGE-$COUNTRY"
        private val MAX_CACHE_SIZE: Long = 10 * 1024 * 1024 // 10MB
        private val OKHTTP_CLIENT = OkHttpClient()
    }
}
