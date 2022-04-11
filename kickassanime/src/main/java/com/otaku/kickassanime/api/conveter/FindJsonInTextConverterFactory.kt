package com.otaku.kickassanime.api.conveter

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.utils.ApiUtils
import com.otaku.kickassanime.exceptions.ApiException
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type


/**
 *
 * Takes index range or regex to find the text to find the json in html
 *
 */
class FindJsonInTextConverterFactory private constructor(private val gson: Gson? = null) :
    Converter.Factory() {

    init {
        if (gson == null) throw NullPointerException("gson == null")
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val mAnnotation = ApiUtils.getJsonInTextAnnotation(annotations)
        if (mAnnotation != null && mAnnotation is JsonInText && gson != null) {
            return converter(type, gson, mAnnotation.field)
        }
        return null
    }

    companion object {
        // :(\[?\{.*\}\])(,\")

        @JvmStatic
        private val regex: Regex = """appData = (\{.*\})(,\")""".toRegex()

        @JvmStatic
        private val converter = value@{ type: Type, gson: Gson, field: String ->
            return@value Converter<ResponseBody, Any> converter@{ responseBody ->
                val rawString = responseBody.string()
                val match = regex.find(rawString)
                val json = match?.groups?.get(1)
                    ?: throw ApiException("No match found for the regex in @JsonInText")
                val objectField = JsonParser().parse(json.value.plus("}"))?.asJsonObject.let { element->
                    return@let if (field == Strings.NONE) element else element?.get(field)
                }
                return@converter gson.fromJson(objectField, type)
            }
        }

        /**
         * Create an instance using a default [Gson] instance for conversion. Encoding to JSON and
         * decoding from JSON (when no charset is specified by a header) will use UTF-8.
         */
        @JvmStatic
        fun create(): FindJsonInTextConverterFactory {
            return create(Gson())
        }

        /**
         * Create an instance using `gson` for conversion. Encoding to JSON and
         * decoding from JSON (when no charset is specified by a header) will use UTF-8.
         */
        @JvmStatic
        fun create(gson: Gson?): FindJsonInTextConverterFactory {
            return FindJsonInTextConverterFactory(gson)
        }
    }
}