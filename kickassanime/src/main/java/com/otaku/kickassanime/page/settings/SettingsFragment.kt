package com.otaku.kickassanime.page.settings

import android.os.Bundle
import com.anggrayudi.materialpreference.PreferenceFragmentMaterial
import com.otaku.kickassanime.R

class SettingsFragment : PreferenceFragmentMaterial() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
    companion object {
        fun newInstance(rootKey: String?) = SettingsFragment().apply {
            arguments = Bundle().also {
                it.putString(ARG_PREFERENCE_ROOT, rootKey)
            }
        }
    }
}