package com.abdulkuddus.talha.newspaper.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.abdulkuddus.talha.newspaper.R

class OnboardingWelcomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val isFirstPage = arguments?.getBoolean("isFirstPage") ?: false

        // Inflate the right layout for this fragment depending on what page
        if (isFirstPage) {

            val rootView = inflater.inflate(R.layout.screen_onboarding_setup, container, false)
            val countries = resources.getStringArray(R.array.pref_country_labels)
            val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_country_select, countries)

            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val countryCodes = resources.getStringArray(R.array.pref_country_values)

            val dropdown: AutoCompleteTextView = rootView.findViewById(R.id.filled_exposed_dropdown)
            dropdown.setAdapter(adapter)
            dropdown.setOnItemClickListener { _, _, position, _ ->
                sharedPrefs.edit {
                    putString(getString(R.string.pref_key_country), countryCodes[position])
                }
            }
            return rootView

        } else {
            return inflater.inflate(R.layout.screen_onboarding_welcome, container, false)
        }

    }

}
