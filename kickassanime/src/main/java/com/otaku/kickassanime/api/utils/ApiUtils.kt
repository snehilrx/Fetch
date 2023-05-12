package com.otaku.kickassanime.api.utils

import com.otaku.kickassanime.api.conveter.JsonInText

object ApiUtils {
    @JvmStatic
    fun getJsonInTextAnnotation(annotations: Array<out Annotation>): Annotation? {
        for (annotation in annotations) {
            if (annotation is JsonInText) return annotation
        }
        return null
    }
}