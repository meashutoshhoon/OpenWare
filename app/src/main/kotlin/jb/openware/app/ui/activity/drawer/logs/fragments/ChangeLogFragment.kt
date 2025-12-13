package jb.openware.app.ui.activity.drawer.logs.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import jb.openware.app.data.LogEntry
import jb.openware.app.data.LogsData
import jb.openware.app.databinding.FragmentChangeLogBinding
import jb.openware.app.databinding.LogsListBinding
import jb.openware.app.ui.components.TextFormatter

class ChangeLogFragment : Fragment() {

    private var _binding: FragmentChangeLogBinding? = null
    private val binding get() = _binding!!

    // Pulls data from LogsData every time (no pointless field caching)
    private val updatesList: List<LogEntry>
        get() = LogsData.logsList

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangeLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() = with(binding.list) {
        layoutManager = LinearLayoutManager(requireContext())
        adapter = ChangeLogAdapter(updatesList, binding.layout1)
        setHasFixedSize(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------- Adapter ----------------

    private class ChangeLogAdapter(
        private val data: List<LogEntry>, private val parentLayout: ViewGroup
    ) : RecyclerView.Adapter<ChangeLogAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = LogsListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding, parentLayout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int = data.size

        class ViewHolder(
            private val binding: LogsListBinding, private val parentLayout: ViewGroup
        ) : RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(entry: LogEntry) = with(binding) {
                appVersion.text = entry.name
                released.text = "Released on: ${entry.releasedOn}"
                TextFormatter.format(log, entry.log)

                contentSH.setOnClickListener {
                    if (hiddenView.isVisible) {
                        collapseView(hiddenView, contentSH)
                    } else {
                        expandView(hiddenView, contentSH)
                    }
                }

                click.setOnClickListener {
                    contentSH.performClick()
                }
            }

            private fun expandView(view: View, arrowImage: ImageView) {
                val rotation = ObjectAnimator.ofFloat(
                    arrowImage, View.ROTATION, 0f, 180f
                ).apply {
                    duration = 300L
                }

                view.isVisible = true
                beginTransition(parentLayout)

                AnimatorSet().apply {
                    playTogether(rotation)
                    start()
                }
            }

            private fun collapseView(view: View, arrowImage: ImageView) {
                val rotation = ObjectAnimator.ofFloat(
                    arrowImage, View.ROTATION, 180f, 0f
                ).apply {
                    duration = 300L
                }

                val initialHeight = view.measuredHeight
                val heightAnimator = ValueAnimator.ofInt(initialHeight, 0).apply {
                    duration = 300L

                    addUpdateListener { animator ->
                        val value = animator.animatedValue as Int
                        view.layoutParams = view.layoutParams.apply {
                            height = value
                        }
                        view.requestLayout()
                    }

                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            view.layoutParams = view.layoutParams.apply {
                                height = ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                            view.isVisible = false
                            view.requestLayout()
                        }
                    })
                }

                AnimatorSet().apply {
                    playTogether(rotation, heightAnimator)
                    start()
                }
            }

            private fun beginTransition(viewGroup: ViewGroup) {
                val transition = AutoTransition().apply {
                    duration = 400L
                    interpolator = DecelerateInterpolator()
                }
                TransitionManager.beginDelayedTransition(viewGroup, transition)
            }
        }
    }
}
