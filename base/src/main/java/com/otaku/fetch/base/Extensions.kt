package com.otaku.fetch.base

import android.content.res.Configuration
import androidx.fragment.app.Fragment

val Any.TAG: String
    get() = this::class.java.simpleName

val Fragment.isLandscape : Boolean
    get() {
        return this.context?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
    }