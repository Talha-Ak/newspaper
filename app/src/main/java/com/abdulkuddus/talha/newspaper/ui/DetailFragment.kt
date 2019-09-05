package com.abdulkuddus.talha.newspaper.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.webkit.WebViewClientCompat
import com.abdulkuddus.talha.newspaper.R
import com.abdulkuddus.talha.newspaper.database.NewsSavedStatus
import com.abdulkuddus.talha.newspaper.databinding.FragmentDetailBinding
import com.abdulkuddus.talha.newspaper.utils.Injector
import com.abdulkuddus.talha.newspaper.utils.networkIsAvailable
import com.abdulkuddus.talha.newspaper.viewmodels.DetailViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class DetailFragment : Fragment() {

    // Get reference to binding object for setting up WebView
    private lateinit var binding: FragmentDetailBinding

    private lateinit var viewModel: DetailViewModel

    // Get navArgs with property delegate
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        // Get ViewModel reference
        val app = requireNotNull(this.activity).application
        viewModel = ViewModelProviders.of(this, Injector.provideDetailViewModelFactory(app, args.newsItem))
            .get(DetailViewModel::class.java)

        // Set toolbar title to be the publisher name
        requireNotNull(this.activity).toolbar.title = viewModel.news.value?.source?.name

        // Observe the Saved Status. When it changes, show a Snackbar informing the user.
        viewModel.isNewsSaved.observe(this, Observer {
            it?.let { showStatusSnackbar(it) }
        })

        viewModel.viewInBrowser.observe(this, Observer {
            it?.let { navigateToBrowser(it) }
        })

        viewModel.shareNews.observe(this, Observer {
            it?.let { shareArticle(it) }
        })

        prepareForWebView()

        return binding.root
    }

    private fun prepareForWebView() {
        if (networkIsAvailable(requireContext())) {
            binding.newsWebview.visibility = View.VISIBLE
            startWebView()
        } else {
            binding.newsWebview.visibility = View.GONE
            Snackbar.make(binding.root, R.string.snackbar_no_internet_connection, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_retry) { prepareForWebView() }
                .show()
        }
    }

    /**
     * Starts up the WebView and configures the Progress Bar, external links and Chrome Custom Tab.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView() {
        with(binding.newsWebview) {
            // Enable JavaScript for the WebView (most sites don't function properly without it)
            settings.javaScriptEnabled = true

            /*
        * Create a new WebViewClient, which controls what happens when a new Url is loaded and what
        * happens when the page has finished loading.
        */
            webViewClient = object : WebViewClientCompat() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    // If the user intentionally navigated to somewhere other than article, use a Chrome Custom Tab.
                    if (request.hasGesture()) {

                        val shouldLaunchInApp = PreferenceManager.getDefaultSharedPreferences(context)
                            .getBoolean(getString(R.string.pref_key_view_extra), true)

                        if (shouldLaunchInApp) {
                            val customTabsIntent = CustomTabsIntent.Builder()
                                .setToolbarColor(
                                    ContextCompat.getColor(context, R.color.primaryColor)
                                )
                                .setStartAnimations(
                                    context,
                                    R.anim.nav_website_enter_anim,
                                    R.anim.nav_webview_exit_anim
                                )
                                .setExitAnimations(
                                    context,
                                    R.anim.nav_webview_enter_anim,
                                    R.anim.nav_website_exit_anim
                                )
                                .build()
                            customTabsIntent.launchUrl(context, request.url)
                            return true
                        } else {
                            viewModel.showInBrowser(request.url)
                        }
                    }
                    return false
                }

                // When the page has finished loading, hide the Progress Bar
                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.progressBar.visibility = View.GONE
                }
            }

            /*
             * Create a new WebChromeClient, which controls other things that we can't access with
             * WebViewClient (i.e. onProgressChanged).
             */
            webChromeClient = object : WebChromeClient() {
                // Update the Progress Bar to indicate how much of the page is loaded.
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.progressBar.setProgress(newProgress, true)
                    } else {
                        binding.progressBar.progress = newProgress
                    }
                }
            }

            // Load the given Url from the News article.
            loadUrl(viewModel.news.value?.url)
        }

    }

    /**
     * Display a Snackbar to the user depending on the save state of the article
     */
    private fun showStatusSnackbar(status: NewsSavedStatus) {
        Snackbar.make(
            binding.root, getString(
                when (status) {
                    NewsSavedStatus.SAVED -> R.string.snackbar_saved_article
                    NewsSavedStatus.NOT_SAVED -> R.string.snackbar_not_saved_article
                    NewsSavedStatus.ERROR -> R.string.snackbar_error_saving_article
                }
            ), Snackbar.LENGTH_LONG
        ).show()
        viewModel.resetSavedStatus()
    }

    private fun navigateToBrowser(url: Intent) {
        // Check if browser available, then start it.
        if (url.resolveActivity(activity!!.packageManager) != null) {
            startActivity(url)
        } else {
            Toast.makeText(context, getString(R.string.no_browser_available), Toast.LENGTH_LONG).show()
        }
        viewModel.browserEventHandled()
    }

    private fun shareArticle(shareIntent: Intent) {
        startActivity(Intent.createChooser(shareIntent, getString(R.string.intent_share_title)))
        viewModel.shareEventHandled()
    }

    /**
     * Inflate detail menu, allowing user to save the article or view in browser.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_save_article -> {
                viewModel.toggleSaveArticle()
                true
            }
            R.id.action_share_article -> {
                viewModel.shareArticle()
                true
            }
            R.id.action_view_in_browser -> {
                viewModel.showInBrowser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up and destroy WebView to avoid memory leaks
        binding.linearLayout.removeAllViews()
        binding.newsWebview.destroy()
    }
}
