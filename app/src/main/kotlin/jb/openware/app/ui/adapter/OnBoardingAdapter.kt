package jb.openware.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jb.openware.app.databinding.ItemContainerOnboardingBinding
import jb.openware.app.ui.items.OnBoardingItem

class OnBoardingAdapter(
    private val items: List<OnBoardingItem>
) : RecyclerView.Adapter<OnBoardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemContainerOnboardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class OnboardingViewHolder(
        private val binding: ItemContainerOnboardingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnBoardingItem) = with(binding) {
            title.text = item.title
            description.text = item.description
            img.setImageResource(item.image)
        }
    }
}
