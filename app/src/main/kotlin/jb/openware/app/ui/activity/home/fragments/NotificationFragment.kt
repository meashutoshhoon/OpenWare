package jb.openware.app.ui.activity.home.fragments

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jb.openware.app.R
import jb.openware.app.databinding.FragmentNotificationBinding
import jb.openware.app.databinding.NotificationCellBinding
import jb.openware.app.ui.components.TextFormatter
import jb.openware.app.ui.items.NotificationItem
import jb.openware.app.util.ConnectionManager
import jb.openware.app.util.notificationUrl
import jb.openware.imageviewer.ImageViewer

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding

    private var listView: RecyclerView? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private lateinit var connectionManager: ConnectionManager

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectionManager = ConnectionManager()

        // RecyclerView created programmatically, but parent & header use viewbinding
        listView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = true
            setPaddingRelative(0, 0, 0, 0)
        }

        refreshLayout = SwipeRefreshLayout(requireContext()).apply {
            addView(listView)
            setOnRefreshListener { refreshData() }
        }

        binding?.layout?.removeAllViews()
        binding?.layout?.addView(
            refreshLayout, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Initial load
        refreshData()
    }

    private fun refreshData() {
        val rl = refreshLayout ?: return
        rl.isRefreshing = true

        connectionManager.startRequest(
            "GET", notificationUrl, "A", object : ConnectionManager.RequestListener {

                override fun onResponse(
                    tag: String, response: String, responseHeaders: HashMap<String, Any>
                ) {
                    if (view == null || !isAdded || _binding == null) return

                    val type = object : TypeToken<List<NotificationItem>>() {}.type
                    val items: List<NotificationItem> = gson.fromJson(response, type)

                    listView?.adapter = NotificationAdapter(items)
                    rl.isRefreshing = false
                }

                override fun onErrorResponse(tag: String, message: String) {
                    if (view == null || !isAdded || _binding == null) return

                    MaterialAlertDialogBuilder(requireActivity()).setTitle("Alert")
                        .setMessage(message).setPositiveButton(android.R.string.ok, null).show()

                    rl.isRefreshing = false
                }
            })
    }

    // Adapter using viewbinding for cell
    private inner class NotificationAdapter(
        private val data: List<NotificationItem>
    ) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = NotificationCellBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            val b = holder.binding
            val ctx = b.root.context

            b.title.text = item.title
            b.message.text = item.message
            b.date.text = item.date

            TextFormatter.format(b.message, item.message)
            b.message.movementMethod = LinkMovementMethod.getInstance()

            if (item.url == "none") {
                b.image.visibility = View.GONE
                b.image.setOnClickListener(null)
            } else {
                b.image.visibility = View.VISIBLE
                val images = listOf(item.url)

                b.image.setOnClickListener { _ ->
                    if (!isAdded) return@setOnClickListener

                    ImageViewer.Builder(ctx, images) { imageViewInner, image ->
                        Glide.with(imageViewInner.context).load(image)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .placeholder(0xFFD3D3D3.toInt().toDrawable())
                            .into(imageViewInner)
                    }
                        .withStartPosition(0)
                        .withHiddenStatusBar(true)
                        .allowZooming(true)
                        .allowSwipeToDismiss(true)
                        .withTransitionFrom(b.image)
                        .withDismissListener { b.image.visibility = View.VISIBLE }
                        .show()
                }

                Glide.with(ctx)
                    .load(item.url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.color.grey)
                    .centerCrop()
                    .into(b.image)
            }
        }

        override fun getItemCount(): Int = data.size

        inner class ViewHolder(val binding: NotificationCellBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    override fun onDestroyView() {
        listView = null
        refreshLayout = null
        _binding = null
        super.onDestroyView()
    }
}