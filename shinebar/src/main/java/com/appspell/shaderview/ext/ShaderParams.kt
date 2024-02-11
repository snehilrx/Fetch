package com.appspell.shaderview.ext

import com.appspell.shaderview.gl.params.ShaderParams
import com.appspell.shaderview.gl.params.TextureOESParam

fun ShaderParams.getTexture2dOESSurfaceTexture(parameterName: String) =
    (this.getParamValue(parameterName) as? TextureOESParam)?.surfaceTexture

fun ShaderParams.getTexture2dOESSurface(parameterName: String) =
    (this.getParamValue(parameterName) as? TextureOESParam)?.surface