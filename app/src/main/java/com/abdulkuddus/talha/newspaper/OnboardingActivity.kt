package com.abdulkuddus.talha.newspaper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import androidx.work.*
import com.abdulkuddus.talha.newspaper.databinding.ActivityOnboardingBinding
import com.abdulkuddus.talha.newspaper.ui.OnboardingWelcomeFragment
import com.abdulkuddus.talha.newspaper.ui.SourcePickerFragment
import com.abdulkuddus.talha.newspaper.utils.Injector
import java.util.concurrent.TimeUnit

class OnboardingActivity : AppCompatActivity() {

    private var currentPosition = 0

    lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        // Inflate the layout for the activity
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)

        binding.apply {
            // Create the adapter for our viewpager
            viewPager.adapter = OnboardingPagerAdapter(supportFragmentManager)
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@OnboardingActivity)

            // If a new page is selected, ensure user has chosen a preference before they can proceed.
            viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    val isValidated = when (currentPosition) {
                        0 -> sharedPrefs.getString(getString(R.string.pref_key_country), null)
                        1 -> sharedPrefs.getString(getString(R.string.pref_key_sources), null)
                        else -> return
                    }
                    if (isValidated != null || currentPosition > position) {
                        viewPager.currentItem = position
                        currentPosition = position
                    } else {
                        viewPager.currentItem = currentPosition
                    }
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    val newProgress = (position + positionOffset) / (viewPager.adapter!!.count - 1)
                    binding.onboardingRoot.progress = newProgress
                }
            })

            onboardingContinueButton.setOnClickListener {
                viewPager.currentItem = viewPager.currentItem + 1
            }
            onboardingPreviousButton.setOnClickListener {
                viewPager.currentItem = viewPager.currentItem - 1
            }
            onboardingCompleteButton.setOnClickListener { if (viewPager.currentItem == 2) finishOnboarding() }

        }
    }

    /*
     * Setup background refresh, start the MainActivity,
     * finish this activity, and ensure this screen won't be shown again using a Preference.
     */
    private fun finishOnboarding() {

        // Ensure background work only occurs inside these constraints.
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresDeviceIdle(true)
            .build()

        // Setup a request to run every 12 hours.
        val updateRequest =
            PeriodicWorkRequest.Builder(UpdateWorker::class.java, 12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueue(updateRequest)

        // Make sure this screen is not shown again.
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.edit { putBoolean(getString(R.string.pref_key_onboarding), false) }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

/**
 * A small implementation of [FragmentPagerAdapter] which tells the viewpager what fragments to show where.
 * Implemented here because it is only used here.
 */
class OnboardingPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> setupOnboardFragment(true)
            1 -> SourcePickerFragment()
            else -> setupOnboardFragment(false)
        }

    }

    override fun getCount() = 3

    private fun setupOnboardFragment(isFirstPage: Boolean): OnboardingWelcomeFragment {
        val bundle = Bundle()
        bundle.putBoolean("isFirstPage", isFirstPage)
        val frag = OnboardingWelcomeFragment()
        frag.arguments = bundle
        return frag
    }

}

/**
 * A [Worker] that updates the news articles in background, used in tandem with WorkManager.
 * Implemented here because it is only used here.
 */
class UpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = Injector.getNewsRepository(applicationContext)
        return if (repository.updateAllInBackground(applicationContext)) {
            Result.success()
        } else {
            Result.failure()
        }

    }
}