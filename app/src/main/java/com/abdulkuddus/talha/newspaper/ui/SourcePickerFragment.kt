package com.abdulkuddus.talha.newspaper.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.databinding.ViewDataBinding
import androidx.databinding.ViewStubProxy
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abdulkuddus.talha.newspaper.NewsRepository.SourceWithPref
import com.abdulkuddus.talha.newspaper.R
import com.abdulkuddus.talha.newspaper.databinding.FragmentSourcePickerBinding
import com.abdulkuddus.talha.newspaper.databinding.SourceListItemBinding
import com.abdulkuddus.talha.newspaper.utils.Injector
import com.abdulkuddus.talha.newspaper.viewmodels.SourcePickerViewModel
import com.google.android.material.chip.ChipGroup

class SourcePickerFragment : Fragment() {

    private lateinit var binding: FragmentSourcePickerBinding
    private lateinit var adapter: SourceAdapter
    private lateinit var viewModel: SourcePickerViewModel

    private var filterCategory: String? = null
    private var filterLanguage: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSourcePickerBinding.inflate(inflater, container, false)

        // Get a reference to our ViewModel, using our Injector to create the factory needed
        val application = requireNotNull(this.activity).application
        viewModel = ViewModelProviders.of(this, Injector.provideSourcePickerViewModelFactory(application))
            .get(SourcePickerViewModel::class.java)

        // Create the adapter and assign it to the RecyclerView
        adapter = SourceAdapter(SourceClick { viewModel.updateSourceChoice(it) })
        binding.sourceRecyclerview.adapter = adapter

        //TODO: Fix flashing when RecyclerView becomes visible
        viewModel.sources.observe(this, Observer {
            // If the list is null/empty, show loading. If not, show list.
            adapter.submitList(it)
            if (it != null && it.isNotEmpty()) {
                binding.progressBar.visibility = View.GONE
                binding.sourceRecyclerview.visibility = View.VISIBLE
            } else {
                binding.sourceRecyclerview.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
        })

        setupFilter()

        return binding.root
    }

    private fun setupFilter() {

        binding.filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            var filteringCategories = false
            val chipGroupContainer: ViewStubProxy?

            /*
            * If the first chip is selected, hide the other chip group, make the selected group visible and setup the
            * click listener for the selected group. If nothing is selected, hide all groups and refresh list if something
            * was selected before.
            */
            when (checkedId) {

                group[0].id -> {
                    chipGroupContainer = binding.categoryContainer
                    filteringCategories = true
                    if (binding.languageContainer.isInflated) binding.languageContainer.root.visibility = View.GONE
                }

                group[1].id -> {
                    chipGroupContainer = binding.languageContainer
                    if (binding.categoryContainer.isInflated) binding.categoryContainer.root.visibility = View.GONE
                }

                else -> {
                    if (binding.categoryContainer.isInflated) binding.categoryContainer.root.visibility = View.GONE
                    if (binding.languageContainer.isInflated) binding.languageContainer.root.visibility = View.GONE
                    if (filterCategory != null || filterLanguage != null) {
                        updateSources(true)
                    }
                    return@setOnCheckedChangeListener
                }
            }

            if (chipGroupContainer.isInflated) {
                chipGroupContainer.root.visibility = View.VISIBLE
            } else {
                chipGroupContainer.viewStub?.visibility = View.VISIBLE
                setupChipGroup(chipGroupContainer.binding!!, filteringCategories)
            }
        }

    }

    private fun setupChipGroup(chipGroupBinding: ViewDataBinding, filteringCategories: Boolean) {

        // Get the correct ChipGroup from the binding object.
        val chipGroup: ChipGroup = chipGroupBinding.root.findViewById(
            if (filteringCategories) {
                R.id.category_chip_group
            } else {
                R.id.language_chip_group
            }
        )

        chipGroup.setOnCheckedChangeListener { group, checkedId ->

            // If the view chosen is NO_ID (i.e. nothing selected) then clear filter, refresh and return.
            if (checkedId == View.NO_ID) {
                if (filteringCategories) {
                    filterCategory = null
                } else {
                    filterLanguage = null
                }
                updateSources()
                return@setOnCheckedChangeListener
            }

            // Get the correct resource depending on the filter selected
            val categories = resources.getStringArray(
                if (filteringCategories) {
                    R.array.filter_category_options
                } else {
                    R.array.filter_language_options
                }
            )

            // When the index of the chip is found, assign that filter to the corresponding var and refresh.
            for (i in categories.indices) {
                if (group[i].id == checkedId) {
                    if (filteringCategories) {
                        filterCategory = categories[i]
                    } else {
                        filterLanguage = categories[i]
                    }
                    updateSources()
                    break
                }
            }
        }
    }

    private fun updateSources(shouldResetAll: Boolean = false) {
        if (shouldResetAll) {
            filterCategory = null
            filterLanguage = null
        }
        viewModel.updateSourceList(filterCategory, filterLanguage)
    }

}

/**
 * RecyclerView Adapter for setting up lottie_onboarding_setup binding on the items in the list.
 * @see [NewsAdapter] for a near exact implementation with comments.
 */
class SourceAdapter(private val clickListener: SourceClick) :
    ListAdapter<SourceWithPref, SourceAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ViewHolder private constructor(val binding: SourceListItemBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        private lateinit var clickListener: SourceClick

        fun bind(item: SourceWithPref, clickListener: SourceClick) {
            this.clickListener = clickListener
            binding.sourceCard.setOnClickListener(this)
            binding.sourceWithPref = item
            binding.executePendingBindings()
        }

        override fun onClick(view: View?) {
            binding.sourceWithPref!!.isSaved = !(binding.sourceWithPref!!.isSaved)
            binding.invalidateAll()
            clickListener.onClick(binding.sourceWithPref!!)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SourceListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

    }

    companion object DiffCallback : DiffUtil.ItemCallback<SourceWithPref>() {
        override fun areItemsTheSame(oldItem: SourceWithPref, newItem: SourceWithPref) =
            oldItem.source.id == newItem.source.id

        override fun areContentsTheSame(oldItem: SourceWithPref, newItem: SourceWithPref) = oldItem == newItem
    }

}

class SourceClick(val clickListener: (sourceItem: SourceWithPref) -> Unit) {
    fun onClick(source: SourceWithPref) = clickListener(source)
}