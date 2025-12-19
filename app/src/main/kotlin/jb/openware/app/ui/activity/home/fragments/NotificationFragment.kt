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
    private val binding get() = _binding ?: error("Attempting to access binding outside of view lifecycle")

    private lateinit var listView: RecyclerView
    private lateinit var refreshLayoutUser: SwipeRefreshLayout
    private lateinit var connectionManager: ConnectionManager

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
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

        refreshLayoutUser = SwipeRefreshLayout(requireContext()).apply {
            addView(listView)
            setOnRefreshListener { refreshData() }
        }

        // layout1 is from fragment_notification.xml, via viewbinding
        binding.layout.addView(
            refreshLayoutUser, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Initial load
        refreshData()
    }

    private fun refreshData() {
        refreshLayoutUser.isRefreshing = true

        connectionManager.startRequest(
            "GET", notificationUrl, "A", object : ConnectionManager.RequestListener {

                override fun onResponse(
                    tag: String, response: String, responseHeaders: HashMap<String, Any>
                ) {
                    val type = object : TypeToken<List<NotificationItem>>() {}.type
                    val items: List<NotificationItem> = gson.fromJson(response, type)

                    listView.adapter = NotificationAdapter(items)
                    refreshLayoutUser.isRefreshing = false
                }

                override fun onErrorResponse(tag: String, message: String) {
                    MaterialAlertDialogBuilder(requireActivity()).setTitle("Alert")
                        .setMessage(message).setPositiveButton(android.R.string.ok, null).show()

                    refreshLayoutUser.isRefreshing = false
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
            val ctx = holder.binding.root.context

            holder.binding.title.text = item.title
            holder.binding.message.text = item.message
            holder.binding.date.text = item.date

            TextFormatter.format(holder.binding.message, item.message)
            holder.binding.message.movementMethod = LinkMovementMethod.getInstance()

            if (item.url == "none") {
                holder.binding.image.visibility = View.GONE
                holder.binding.image.setOnClickListener(null)
            } else {
                holder.binding.image.visibility = View.VISIBLE

                val images = listOf(item.url)

                holder.binding.image.setOnClickListener { imageView ->
                    ImageViewer.Builder(
                        imageView.context, images
                    ) { imageViewInner, image ->
                        Glide.with(imageViewInner.context).load(image)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .placeholder(0xFFD3D3D3.toInt().toDrawable()).into(imageViewInner)
                    }.withStartPosition(0).withHiddenStatusBar(true).allowZooming(true)
                        .allowSwipeToDismiss(true).withTransitionFrom(holder.binding.image)
                        .withDismissListener {
                            holder.binding.image.visibility = View.VISIBLE
                        }.show()
                }

                Glide.with(ctx).load(item.url).transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.color.grey).centerCrop().into(holder.binding.image)
            }
        }

        override fun getItemCount(): Int = data.size

        inner class ViewHolder(val binding: NotificationCellBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}