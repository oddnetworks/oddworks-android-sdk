package io.oddworks.device.request

import android.util.Log
import android.webkit.MimeTypeMap
import io.oddworks.device.exception.OddParseException
import io.oddworks.device.model.*
import io.oddworks.device.model.common.*
import io.oddworks.device.model.config.Display
import io.oddworks.device.model.config.Features
import io.oddworks.device.model.config.features.Authentication
import io.oddworks.device.model.config.features.Metric
import io.oddworks.device.model.video.OddCast
import io.oddworks.device.model.video.OddSource
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object OddParser {
    private val DATA = "data"
    private val ERRORS = "errors"
    private val ATTRIBUTES = "attributes"
    private val META = "meta"
    private val INCLUDED = "included"
    private val ID = "id"
    private val TYPE = "type"
    private val RELATIONSHIPS = "relationships"
    private val TAG = OddParser::class.java.simpleName

    @Throws(JSONException::class)
    fun parseMultipleResponse(responseBody: String): LinkedHashSet<OddResource> {
        val rawBody = JSONObject(responseBody)
        val rawData = SafeJSONParser.getJSONArray(rawBody, DATA, true)!!
        return parseResourceArray(rawData)
    }

    @Throws(JSONException::class, IllegalArgumentException::class, OddParseException::class)
    fun parseSingleResponse(responseBody: String): OddResource {
        val rawBody = JSONObject(responseBody)
        val rawData = SafeJSONParser.getJSONObject(rawBody, DATA, true)!!
        val rawIncluded = SafeJSONParser.getJSONArray(rawBody, INCLUDED, false)
        val rawType = SafeJSONParser.getString(rawData, TYPE)?.toUpperCase() ?: throw OddParseException("parseSingleResponse() unable to determine resource type")

        var resourceType: OddResourceType? = null
        try {
            resourceType = OddResourceType.valueOf(rawType)
        } catch (e: Exception) {
            Log.w(TAG, "Unknown OddResourceType: ${e.message}")
        }

        return when (resourceType) {
            OddResourceType.COLLECTION -> parseCollection(rawData, rawIncluded)
            OddResourceType.CONFIG -> parseConfig(rawData)
            OddResourceType.PROMOTION -> parsePromotion(rawData, rawIncluded)
            OddResourceType.VIDEO -> parseVideo(rawData, rawIncluded)
            OddResourceType.VIEW -> parseView(rawData, rawIncluded)
            OddResourceType.VIEWER -> parseViewer(rawData, rawIncluded)
            else -> {
                throw OddParseException("Attempting to parse unknown OddResourceType")
            }
        }

    }

    @Throws(JSONException::class)
    fun parseErrorMessage(responseBody: String): LinkedHashSet<OddError> {
        val rawBody = JSONObject(responseBody)
        val rawErrors = SafeJSONParser.getJSONArray(rawBody, ERRORS, true)!!
        val errors = linkedSetOf<OddError>()

        for(i in 0..(rawErrors.length() -1)) {
            val rawError = rawErrors.getJSONObject(i)

            val id = SafeJSONParser.getString(rawError, ID) ?: ""
            val status = SafeJSONParser.getString(rawError, "status") ?: ""
            val code = SafeJSONParser.getString(rawError, "code") ?: ""
            val title = SafeJSONParser.getString(rawError, "title") ?: ""
            val detail = SafeJSONParser.getString(rawError, "detail") ?: ""
            val meta = SafeJSONParser.getJSONObject(rawError, META, false)

            errors.add(OddError(id, status, code, title, detail, meta))
        }

        return errors
    }

    @Throws(JSONException::class, IllegalArgumentException::class, OddParseException::class)
    private fun parseResourceArray(rawArray: JSONArray?): LinkedHashSet<OddResource> {
        val resources = linkedSetOf<OddResource>()
        if (rawArray == null) {
            return resources
        }
        for (i in 0..rawArray.length() - 1) {
            val includedObject = rawArray.getJSONObject(i)
            val rawType = SafeJSONParser.getString(includedObject, TYPE)?.toUpperCase() ?: throw OddParseException("parseResourceArray() unable to determine resource type")

            var resourceType: OddResourceType? = null
            try {
                resourceType = OddResourceType.valueOf(rawType)
            } catch (e: Exception) {
                Log.w(TAG, "Unknown OddResourceType: ${e.message}")
            }

            when (resourceType) {
                OddResourceType.COLLECTION -> resources.add(parseCollection(includedObject))
                OddResourceType.PROMOTION -> resources.add(parsePromotion(includedObject))
                OddResourceType.VIDEO -> resources.add(parseVideo(includedObject))
                OddResourceType.VIEW -> resources.add(parseView(includedObject))
                OddResourceType.CONFIG -> resources.add(parseConfig(includedObject))
                else -> {
                    throw OddParseException("Attempting to parse unknown OddResourceType")
                }
            }
        }

        return resources
    }

    @Throws(JSONException::class, IllegalArgumentException::class)
    private fun parseConfig(rawData: JSONObject): OddConfig {
        val id = SafeJSONParser.getString(rawData, ID) ?: throw OddParseException("parseConfig() missing id property")

        val rawAttributes = SafeJSONParser.getJSONObject(rawData, ATTRIBUTES, false)
        val meta = SafeJSONParser.getJSONObject(rawData, META, false)

        val views = mutableMapOf<String, String>()
        val rawViews = SafeJSONParser.getJSONObject(rawAttributes, "views", false)
        if (rawViews != null) {
            val viewNames = rawViews.keys()
            while (viewNames.hasNext()) {
                val viewName = viewNames.next()
                val viewId = SafeJSONParser.getString(rawViews, viewName) ?: throw OddParseException("parseConfig() missing View id")
                views.put(viewName, viewId)
            }
        }

        val display = parseDisplay(rawAttributes)
        val features = parseFeatures(rawAttributes)

        return OddConfig(id, OddResourceType.CONFIG, meta, views, display, features)
    }

    @Throws(JSONException::class)
    private fun parseViewer(rawData: JSONObject, rawIncluded: JSONArray? = null): OddViewer {
        val id = SafeJSONParser.getString(rawData, ID) ?: throw OddParseException("parseConfig() missing id property")

        val rawAttributes = SafeJSONParser.getJSONObject(rawData, ATTRIBUTES, false)
        val meta = SafeJSONParser.getJSONObject(rawData, META, false)

        val email = SafeJSONParser.getString(rawAttributes, "email") ?: ""
        val jwt = SafeJSONParser.getString(rawAttributes, "jwt") ?: ""

        val entitlements = mutableSetOf<String>()
        val rawEntitlements = SafeJSONParser.getJSONArray(rawAttributes, "entitlements", false)
        if (rawEntitlements != null) {
            for (i in 0..(rawEntitlements.length() - 1)) {
                val entitlement = rawEntitlements.getString(i)
                entitlements.add(entitlement)
            }
        }


        val rawRelationships = SafeJSONParser.getJSONObject(rawData, RELATIONSHIPS, false)
        val relationships = parseRelationships(rawRelationships)

        val included = parseResourceArray(rawIncluded)

        return OddViewer(id, OddResourceType.VIEWER, relationships, included, meta, email, entitlements.toSet(), jwt)
    }

    @Throws(JSONException::class)
    private fun parseCollection(rawData: JSONObject, rawIncluded: JSONArray? = null): OddCollection {
        val id = SafeJSONParser.getString(rawData, ID) ?: throw OddParseException("parseCollection() missing id property")

        val rawAttributes = SafeJSONParser.getJSONObject(rawData, ATTRIBUTES, false)
        val meta = SafeJSONParser.getJSONObject(rawData, META, false)
        val rawImages = SafeJSONParser.getJSONArray(rawAttributes, "images", false)

        val title = SafeJSONParser.getString(rawAttributes, "title") ?: ""
        val description = SafeJSONParser.getString(rawAttributes, "description") ?: ""
        val releaseDate = SafeJSONParser.getDate(rawAttributes, "releaseDate")
        val images = parseImages(rawImages)

        val rawRelationships = SafeJSONParser.getJSONObject(rawData, RELATIONSHIPS, false)
        val relationships = parseRelationships(rawRelationships)

        val rawGenres = SafeJSONParser.getJSONArray(rawAttributes, "genres", false)
        val genres = parseGenres(rawGenres)

        val included = parseResourceArray(rawIncluded)

        return OddCollection(
                id,
                OddResourceType.COLLECTION,
                relationships,
                included,
                meta,
                title,
                description,
                images,
                genres,
                releaseDate)
    }

    @Throws(JSONException::class)
    private fun parseVideo(rawData: JSONObject, rawIncluded: JSONArray? = null): OddVideo {
        val id = SafeJSONParser.getString(rawData, ID) ?: throw OddParseException("parseVideo() missing id property")

        val rawAttributes = SafeJSONParser.getJSONObject(rawData, ATTRIBUTES, false)
        val meta = SafeJSONParser.getJSONObject(rawData, META, false)
        val rawImages = SafeJSONParser.getJSONArray(rawAttributes, "images", false)

        val title = SafeJSONParser.getString(rawAttributes, "title") ?: ""
        val description = SafeJSONParser.getString(rawAttributes, "description") ?: ""
        val images = parseImages(rawImages)
        val releaseDate = SafeJSONParser.getDate(rawAttributes, "releaseDate")
        val duration = SafeJSONParser.getInt(rawAttributes, "duration")
        val position = SafeJSONParser.getInt(rawAttributes, "position")
        val complete = SafeJSONParser.getBoolean(rawAttributes, "complete")

        val rawSources = SafeJSONParser.getJSONArray(rawAttributes, "sources", false)
        val sources = parseSources(rawSources)

        val rawRelationships = SafeJSONParser.getJSONObject(rawData, RELATIONSHIPS, false)
        val relationships = parseRelationships(rawRelationships)

        val rawGenres = SafeJSONParser.getJSONArray(rawAttributes, "genres", false)
        val genres = parseGenres(rawGenres)


        val rawCast = SafeJSONParser.getJSONArray(rawAttributes, "cast", false)
        val cast = parseCast(rawCast)
        val rawTags = SafeJSONParser.getJSONArray(rawAttributes, "tags", false)
        val tags = parseTags(rawTags)

        val included = parseResourceArray(rawIncluded)

        return OddVideo(
                id,
                OddResourceType.VIDEO,
                relationships,
                included,
                meta,
                title,
                description,
                images,
                sources,
                duration,
                genres,
                cast,
                tags,
                releaseDate,
                position,
                complete)
    }

    @Throws(JSONException::class)
    private fun parsePromotion(rawData: JSONObject, rawIncluded: JSONArray? = null): OddPromotion {
        val id = SafeJSONParser.getString(rawData, ID) ?: throw OddParseException("parseVideo() missing id property")

        val rawAttributes = SafeJSONParser.getJSONObject(rawData, ATTRIBUTES, false)
        val meta = SafeJSONParser.getJSONObject(rawData, META, false)
        val rawImages = SafeJSONParser.getJSONArray(rawAttributes, "images", false)

        val title = SafeJSONParser.getString(rawAttributes, "title") ?: ""
        val description = SafeJSONParser.getString(rawAttributes, "description") ?: ""
        val url = SafeJSONParser.getString(rawAttributes, "url") ?: ""
        val images = parseImages(rawImages)

        val rawRelationships = SafeJSONParser.getJSONObject(rawData, RELATIONSHIPS, false)
        val relationships = parseRelationships(rawRelationships)

        val included = parseResourceArray(rawIncluded)

        return OddPromotion(id, OddResourceType.PROMOTION, relationships, included, meta, title, description, images, url)
    }

    @Throws(JSONException::class)
    private fun parseView(rawData: JSONObject, rawIncluded: JSONArray? = null): OddView {
        val id = SafeJSONParser.getString(rawData, ID) ?: throw OddParseException("parseVideo() missing id property")

        val rawAttributes = SafeJSONParser.getJSONObject(rawData, ATTRIBUTES, false)
        val meta = SafeJSONParser.getJSONObject(rawData, META, false)
        val rawImages = SafeJSONParser.getJSONArray(rawAttributes, "images", false)

        val images = parseImages(rawImages)

        val title = SafeJSONParser.getString(rawAttributes, "title") ?: ""

        val rawRelationships = SafeJSONParser.getJSONObject(rawData, RELATIONSHIPS, false)
        val relationships = parseRelationships(rawRelationships)

        val included = parseResourceArray(rawIncluded)

        return OddView(
                id,
                OddResourceType.VIEW,
                relationships,
                included,
                meta,
                title,
                images
                )
    }

    @Throws(JSONException::class)
    private fun parseDisplay(rawAttributes: JSONObject?): Display {
        if (rawAttributes == null) {
            return Display(emptySet(), emptySet(), emptySet())
        }
        val rawDisplay = SafeJSONParser.getJSONObject(rawAttributes, "display", false)

        if (rawDisplay != null) {
            val rawImages = SafeJSONParser.getJSONArray(rawDisplay, "images", false)
            val rawColors = SafeJSONParser.getJSONArray(rawDisplay, "colors", false)
            val rawFonts = SafeJSONParser.getJSONArray(rawDisplay, "fonts", false)
            return Display(parseImages(rawImages), parseColors(rawColors), parseFonts(rawFonts))
        }

        return Display(emptySet(), emptySet(), emptySet())
    }

    @Throws(JSONException::class)
    private fun parseImages(rawImages: JSONArray?): Set<OddImage> {
        if (rawImages == null) {
            return emptySet()
        }
        val images = mutableSetOf<OddImage>()

        loop@for(i in 0..(rawImages.length() -1)) {
            val rawImage = rawImages.getJSONObject(i)

            val url = SafeJSONParser.getString(rawImage, "url")
            var rawMimeType = SafeJSONParser.getString(rawImage, "mimeType")
            if (rawMimeType == null && url != null) {
               val extension = MimeTypeMap.getFileExtensionFromUrl(url)
                rawMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            if (rawMimeType != null && url != null) {
                val mimeType = OddImage.MimeType.valueFromMimeType(rawMimeType)
                if (mimeType == null) {
                    Log.w(TAG, "Unknown image mimeType - skipping")
                    continue@loop
                }

                val width = SafeJSONParser.getInt(rawImage, "width")
                val height = SafeJSONParser.getInt(rawImage, "height")
                val label = SafeJSONParser.getString(rawImage, "label") ?: ""

                images.add(OddImage(url, mimeType, width, height, label))
            } else {
                Log.w(TAG, "No image url ($url) or image mimeType ($rawMimeType) - skipping")
                continue@loop
            }
        }

        return images.toSet()
    }

    @Throws(JSONException::class)
    private fun parseColors(rawColors: JSONArray?): Set<OddColor> {
        if (rawColors == null) {
            return emptySet()
        }
        val colors = mutableSetOf<OddColor>()

        for(i in 0..(rawColors.length() -1)) {
            val rawColor = rawColors.getJSONObject(i)

            val red = SafeJSONParser.getInt(rawColor, "red")
            val green = SafeJSONParser.getInt(rawColor, "green")
            val blue = SafeJSONParser.getInt(rawColor, "blue")
            val alpha = SafeJSONParser.getInt(rawColor, "alpha")
            val label = SafeJSONParser.getString(rawColor, "label") ?: ""

            colors.add(OddColor(red, green, blue, alpha, label))
        }

        return colors.toSet()
    }

    @Throws(JSONException::class)
    private fun parseFonts(rawFonts: JSONArray?): Set<OddFont> {
        if (rawFonts == null) {
            return emptySet()
        }
        val fonts = mutableSetOf<OddFont>()

        for(i in 0..(rawFonts.length() -1)) {
            val rawFont = rawFonts.getJSONObject(i)

            val name = SafeJSONParser.getString(rawFont, "name") ?: ""
            val size = SafeJSONParser.getInt(rawFont, "size")
            val label = SafeJSONParser.getString(rawFont, "label") ?: ""

            fonts.add(OddFont(name, size, label))
        }

        return fonts.toSet()
    }

    private fun parseFeatures(rawAttributes: JSONObject?): Features {
        if (rawAttributes == null) {
            return Features(Authentication(false, Authentication.AuthenticationType.DISABLED), Sharing(false), emptySet(), false)
        }
        val rawFeatures = SafeJSONParser.getJSONObject(rawAttributes, "features", false) ?: return Features(Authentication(false, Authentication.AuthenticationType.DISABLED), Sharing(false), emptySet(), false)

        val metricsConfig = parseMetricsFeature(rawFeatures)

        return Features(parseAuthenticationFeature(rawFeatures), parseSharingFeature(rawFeatures), metricsConfig.first, metricsConfig.second)
    }

    @Throws(JSONException::class, IllegalArgumentException::class)
    private fun parseAuthenticationFeature(rawFeatures: JSONObject): Authentication {
        val rawAuthentication = SafeJSONParser.getJSONObject(rawFeatures, "authentication", false) ?: return Authentication(false, Authentication.AuthenticationType.DISABLED)

        val enabled = SafeJSONParser.getBoolean(rawAuthentication, "enabled")
        val type = SafeJSONParser.getString(rawAuthentication, TYPE)?.toUpperCase() ?: "DISABLED"


        var authenticationType: Authentication.AuthenticationType
        try {
            authenticationType = Authentication.AuthenticationType.valueOf(type)
        } catch (e: Exception) {
            Log.w(TAG, "Unknown Authentication.AuthenticationType: ${e.message}")
            authenticationType = Authentication.AuthenticationType.DISABLED
        }

        val properties = mutableMapOf<String, String>()
        rawAuthentication.keys().forEach {
            when (it) {
                "enabled" -> {}
                TYPE -> {}
                else -> {
                    properties.put(it, SafeJSONParser.getString(rawAuthentication, it) ?: "")
                }
            }
        }


        return Authentication(enabled, authenticationType, properties)
    }

    @Throws(JSONException::class)
    private fun parseSharingFeature(rawFeatures: JSONObject): Sharing {
        val rawSharing = SafeJSONParser.getJSONObject(rawFeatures, "sharing", false) ?: return Sharing(false)
        return parseSharing(rawSharing)
    }

    @Throws(JSONException::class)
    private fun parseMetricsFeature(rawFeatures: JSONObject): Pair<Set<Metric>, Boolean> {
        val rawMetrics = SafeJSONParser.getJSONObject(rawFeatures, "metrics", false) ?: return Pair(emptySet(), false)

        val enabled = SafeJSONParser.getBoolean(rawMetrics, "enabled")

        val metrics = mutableSetOf<Metric>()
        Metric.MetricType.values().forEach {
            val rawMetric = SafeJSONParser.getJSONObject(rawMetrics, it.key, false) ?: return@forEach
            val individualEnabled = SafeJSONParser.getBoolean(rawMetric, "enabled")
            val action = SafeJSONParser.getString(rawMetric, "action")
            val interval = SafeJSONParser.getInt(rawMetric, "interval")
            metrics.add(Metric(it, individualEnabled, action, interval))
        }

        Metric.setupOddMetrics(metrics.toSet()) // MAGIC!

        return Pair(metrics, enabled)

    }

    @Throws(JSONException::class)
    private fun parseSharing(rawSharing: JSONObject): Sharing {
        val enabled = SafeJSONParser.getBoolean(rawSharing, "enabled")
        val text = SafeJSONParser.getString(rawSharing, "text") ?: ""
        return Sharing(enabled, text)
    }

    @Throws(JSONException::class)
    private fun parseGenres(rawGenres: JSONArray?): MutableSet<String> {
        val genres = mutableSetOf<String>()
        if (rawGenres != null) {
            (0..(rawGenres.length() -1)).mapNotNullTo(genres) { rawGenres.getString(it) }
        }
        return genres
    }

    @Throws(JSONException::class)
    private fun parseSources(rawSources: JSONArray?): MutableSet<OddSource> {
        val sources = mutableSetOf<OddSource>()
        if (rawSources != null) {
            loop@for (i in 0..(rawSources.length() -1)) {
                val rawSource = rawSources.getJSONObject(i)

                val url = SafeJSONParser.getString(rawSource, "url")
                val rawMimeType = SafeJSONParser.getString(rawSource, "mimeType")
                if (url == null || rawMimeType == null) {
                    Log.w(TAG, "Unknown source url ($url) or source mimeType ($rawMimeType) - skipping")
                    continue@loop
                } else {
                    val container = SafeJSONParser.getString(rawSource, "container") ?: ""
                    val label = SafeJSONParser.getString(rawSource, "label") ?: ""
                    val rawSourceType = SafeJSONParser.getString(rawSource, "sourceType")
                    val broadcasting = SafeJSONParser.getBoolean(rawSource, "broadcasting")

                    val sourceType = if (rawSourceType == null) {
                        Log.d(TAG, "source missing sourceType - default VOD")
                        OddSource.SourceType.VOD
                    } else {
                        try {
                            OddSource.SourceType.valueOf(rawSourceType)
                        } catch (e: IllegalArgumentException) {
                            Log.d(TAG, "source unknown sourceType - default VOD")
                            OddSource.SourceType.VOD
                        }
                    }

                    val mimeType = OddSource.MimeType.valueFromMimeType(rawMimeType)

                    if (mimeType == null) {
                        Log.w(TAG, "source mimeType ($rawMimeType) not valid - skipping")
                        continue@loop
                    }

                    sources.add(OddSource(url, container, mimeType, label, sourceType, broadcasting))
                }
            }
        }

        return sources
    }

    @Throws(JSONException::class)
    private fun parseCast(rawCast: JSONArray?): MutableSet<OddCast> {
        val cast = mutableSetOf<OddCast>()
        if (rawCast != null) {
            for (i in 0..(rawCast.length() -1)) {
                val rawCastMember = rawCast.getJSONObject(i)

                val name = SafeJSONParser.getString(rawCastMember, "name") ?: ""
                val role = SafeJSONParser.getString(rawCastMember, "role") ?: ""
                val character = SafeJSONParser.getString(rawCastMember, "character") ?: ""

                cast.add(OddCast(name, role, character))
            }
        }

        return cast
    }

    @Throws(JSONException::class)
    private fun parseTags(rawTags: JSONArray?): MutableSet<String> {
        val tags = mutableSetOf<String>()
        if (rawTags != null) {
            (0..(rawTags.length() -1)).mapNotNullTo(tags) { rawTags.getString(it) }
        }
        return tags
    }

//    fun parseSearch(result: String): List<OddObject> {
//        val searchResult = ArrayList<OddObject>()
//        try {
//            val resultObject = JSONObject(result)
//            val dataArray = resultObject.getJSONArray(DATA)
//            for (i in 0..dataArray.length() - 1) {
//                val item = dataArray.getJSONObject(i)
//                val type = SafeJSONParser.getString(item, TYPE)
//
//                when (type) {
//                    OddObject.TYPE_VIDEO, OddObject.TYPE_LIVE_STREAM -> {
//                        val video = parseMedia(item)
//                        if (video != null) {
//                            searchResult.add(video)
//                        }
//                    }
//                    OddObject.TYPE_COLLECTION -> {
//                        val collection = parseCollection(item)
//                        if (collection != null) {
//                            searchResult.add(collection)
//                        }
//                    }
//                }
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//
//        return searchResult
//    }

    @Throws(JSONException::class, IllegalArgumentException::class)
    private fun parseRelationships(rawRelationships: JSONObject?): MutableSet<OddRelationship> {
        val relationships = mutableSetOf<OddRelationship>()
        if (rawRelationships == null) {
            return relationships
        }
        val relationshipNames = rawRelationships.keys()
        while (relationshipNames.hasNext()) {
            val name = relationshipNames.next()
            val relationship = SafeJSONParser.getJSONObject(rawRelationships, name, true)
            if (relationship != null) {
                val identifiers = linkedSetOf<OddIdentifier>()

                if (relationship.get(DATA) is JSONObject) {
                    //handle single relationship.data
                    val identifier = SafeJSONParser.getJSONObject(relationship, DATA, true)!!
                    val relId = SafeJSONParser.getString(identifier, ID) ?: throw OddParseException("parseRelationships() missing id property")
                    val relType = SafeJSONParser.getString(identifier, TYPE)?.toUpperCase() ?: throw OddParseException("parseRelationships() missing type property")
                    try {
                        identifiers.add(OddIdentifier(relId, OddResourceType.valueOf(relType)))
                    } catch (e: Exception) {
                        Log.w(TAG, "Unknown OddResourceType: ${e.message}")
                    }
                } else if (relationship.get(DATA) is JSONArray) {
                    // handle multiple relationship.data
                    val rawIdentifiers = SafeJSONParser.getJSONArray(relationship, DATA, true)!!

                    for (i in 0..rawIdentifiers.length() - 1) {
                        val identifier = rawIdentifiers.getJSONObject(i)
                        val relId = SafeJSONParser.getString(identifier, ID) ?: throw OddParseException("parseRelationships() missing id property")
                        val relType = SafeJSONParser.getString(identifier, TYPE)?.toUpperCase() ?: throw OddParseException("parseRelationships() missing type property")
                        try {
                            identifiers.add(OddIdentifier(relId, OddResourceType.valueOf(relType)))
                        } catch (e: Exception) {
                            Log.w(TAG, "Unknown OddResourceType: ${e.message}")
                        }
                    }
                }

                relationships.add(OddRelationship(name, identifiers))
            }
        }
        return relationships
    }

}
