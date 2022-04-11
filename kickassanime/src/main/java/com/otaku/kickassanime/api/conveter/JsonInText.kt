package com.otaku.kickassanime.api.conveter

import com.otaku.kickassanime.Strings

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class JsonInText(
    val field: String = Strings.NONE
)
