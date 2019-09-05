package com.abdulkuddus.talha.newspaper.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.abdulkuddus.talha.newspaper.R
import com.abdulkuddus.talha.newspaper.databinding.FragmentLocalNewsBinding
import com.abdulkuddus.talha.newspaper.network.NewsFetchStatus
import com.abdulkuddus.talha.newspaper.utils.Injector
import com.abdulkuddus.talha.newspaper.viewmodels.LocalViewModel
import com.google.android.material.snackbar.Snackbar

class LocalNewsFragment : Fragment() {

    private lateinit var viewModel: LocalViewModel
    private lateinit var adapter: NewsAdapter
    private lateinit var binding: FragmentLocalNewsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        binding = FragmentLocalNewsBinding.inflate(inflater, container, false)

        // Get a reference to our ViewModel, passing in the application context so it can create the database it needs
        val application = requireNotNull(this.activity).application
        viewModel = ViewModelProviders.of(this, Injector.provideLocalViewModelFactory(application))
            .get(LocalViewModel::class.java)

        // Create and assign the adapter to the RecyclerView
        adapter = NewsAdapter(NewsClick {
            viewModel.displayNewsArticle(it)
        })
        binding.localNewsList.adapter = adapter

        binding.localSwipeRefresh.setColorSchemeResources(R.color.secondaryColor, R.color.primaryColor)
        binding.localSwipeRefresh.setOnRefreshListener { viewModel.refreshNews() }

        observeLiveData()

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun observeLiveData() {
        // Observe the News List of the ViewModel. When it updates, submit it to the adapter.
        viewModel.news.observe(this, Observer {
            Log.i("LocalFrag", "Observer triggered")
            Log.i("LocalFrag", "status: ${it.status}. articles: ${it.articles}")
            when (it.status) {
                NewsFetchStatus.OK -> {
                    if (it.articles.isNullOrEmpty()) {
                        viewModel.refreshNews()
                        binding.localSwipeRefresh.isRefreshing = true
                        return@Observer
                    }
                    adapter.submitList(it.articles)
                }

                NewsFetchStatus.NO_INTERNET -> {
                    Snackbar.make(binding.root, R.string.snackbar_no_internet_connection, Snackbar.LENGTH_LONG).show()
                    viewModel.setListAsHandled()
                }

                NewsFetchStatus.ERROR -> {
                    Snackbar.make(binding.root, R.string.snackbar_error_refreshing, Snackbar.LENGTH_LONG).show()
                    viewModel.setListAsHandled()
                }
                NewsFetchStatus.HANDLED -> {
                    if (adapter.itemCount == 0 && !it.articles.isNullOrEmpty()) {
                        adapter.submitList(it.articles)
                    }
                    return@Observer
                }
            }
            binding.localSwipeRefresh.isRefreshing = false
        })

        // Observe navigateToArticle and when it isn't null, navigate.
        // After navigating, reset it to null
        viewModel.navigateToArticle.observe(this, Observer {
            if (null != it) {
                this.findNavController().navigate(
                    LocalNewsFragmentDirections.actionGlobalDetailFragment(
                        it
                    )
                )
                viewModel.displayNewsArticleComplete()
            }
        })

        viewModel.navigateToSettings.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate == true) {
                findNavController().navigate(LocalNewsFragmentDirections.actionGlobalPreferenceFragment())
                viewModel.displaySettingsComplete()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                viewModel.displaySettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
