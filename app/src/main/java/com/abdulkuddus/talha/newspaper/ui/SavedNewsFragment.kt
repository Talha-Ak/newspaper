package com.abdulkuddus.talha.newspaper.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.abdulkuddus.talha.newspaper.R
import com.abdulkuddus.talha.newspaper.databinding.FragmentSavedNewsBinding
import com.abdulkuddus.talha.newspaper.utils.Injector
import com.abdulkuddus.talha.newspaper.viewmodels.SavedViewModel

class SavedNewsFragment : Fragment() {

    private lateinit var binding: FragmentSavedNewsBinding
    private lateinit var viewModel: SavedViewModel
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        binding = FragmentSavedNewsBinding.inflate(inflater, container, false)

        // Get a reference to our ViewModel, passing in the application context so it can create the database it needs
        val application = requireNotNull(this.activity).application
        viewModel = ViewModelProviders.of(this, Injector.provideSavedViewModelFactory(application))
            .get(SavedViewModel::class.java)

        // Create and assign the adapter to the RecyclerView
        adapter = NewsAdapter(NewsClick {
            viewModel.displayNewsArticle(it)
        })
        binding.savedNewsList.adapter = adapter

        observeLiveData()

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun observeLiveData() {
        // Observe the News List of the ViewModel. When it updates, submit it to the adapter.
        viewModel.news.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                binding.savedNewsList.visibility = View.GONE
                binding.noSavedNewsLayout.visibility = View.VISIBLE
            } else {
                adapter.submitList(it)
                binding.noSavedNewsLayout.visibility = View.GONE
                binding.savedNewsList.visibility = View.VISIBLE
            }
        })

        // Observe navigateToArticle and when it isn't null, navigate.
        // After navigating, reset it to null
        viewModel.navigateToArticle.observe(this, Observer {
            if (null != it) {
                this.findNavController().navigate(
                    SavedNewsFragmentDirections.actionGlobalDetailFragment(
                        it
                    )
                )
                viewModel.displayNewsArticleComplete()
            }
        })

        viewModel.navigateToSettings.observe(this, Observer { shouldNavigate ->
            // shouldNavigate can be null, hence the seemingly pointless "= true"
            if (shouldNavigate == true) {
                findNavController().navigate(SavedNewsFragmentDirections.actionGlobalPreferenceFragment())
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