package com.otaku.fetch

import androidx.annotation.DrawableRes

object ModuleRegistry {

    data class ModuleData constructor(
        val displayName: String
    ) {
        @DrawableRes
        var displayIcon: Int? = null
        var appModule: AppModule? = null

        constructor(
            displayName: String,
            @DrawableRes displayIcon: Int,
            appModule: AppModule
        ) : this(displayName) {
            this.displayIcon = displayIcon
            this.appModule = appModule
        }
    }

    @JvmStatic
    val modules = HashMap<String, ModuleData>()

    /**
     * Registers a [AppModule]
     * For each module create a static block that calls this function.
     * After all the module is registered main activity (app module) it displays all the available
     * modules. When user selects a module we launch the module activity (app module) with the
     * named tag of the module, to inject the module dynamically at run time.
     * @param displayName injected name tag of AppModule
     * */
    fun registerModule(displayName: String, @DrawableRes displayIcon: Int, appModule: AppModule) {
        modules[displayName] = ModuleData(displayName, displayIcon, appModule)
    }

    fun getModulesList(): List<ModuleData> {
        return modules.values.toList()
    }
}