package com.abdulkuddus.talha.newspaper.ui


import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.abdulkuddus.talha.newspaper.R

class PreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // Inflate and set preferences from the given resource.
        setPreferencesFromResource(R.xml.pref_main, rootKey)

        val articleSwitch = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_view_article))

        // If it is disabled, uncheck the external switch automatically to avoid confusion
        articleSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (!(newValue as Boolean)) {
                val extraPref = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_view_extra))
                extraPref?.isChecked = false
            }
            true
        }

        // Set the source preference to navigate to our source picker screen that is also shown in onboarding.
        //TODO: Find a way to prevent user from deselecting all their sources.
        val sourcesPref = findPreference<Preference>(getString(R.string.pref_key_sources))
        sourcesPref?.setOnPreferenceClickListener {
            findNavController().navigate(PreferenceFragmentDirections.actionPreferenceFragmentToSourcePickerFragment())
            true
        }
    }

}
