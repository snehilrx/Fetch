package com.appspell.shaderview.gl.params

/**
 * @hide
 */
data class Param(
    val valeType: ValueType,
    var location: Int = UNKNOWN_LOCATION,
    var value: Any? = null
) {
    enum class ValueType {
        FLOAT, INT, BOOL,
        FLOAT_VEC2, FLOAT_VEC3, FLOAT_VEC4,
        INT_VEC2, INT_VEC3, INT_VEC4,
        MAT3, MAT4, MAT3x4,
        SAMPLER_2D, SAMPLER_OES
    }
}