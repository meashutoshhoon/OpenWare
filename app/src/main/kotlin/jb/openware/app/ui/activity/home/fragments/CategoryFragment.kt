package jb.openware.app.ui.activity.home.fragments

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jb.openware.app.databinding.CategoryCellBinding
import jb.openware.app.databinding.FragmentCategoryBinding
import jb.openware.app.ui.activity.other.CategoryActivity
import jb.openware.app.ui.items.CategoryItem
import jb.openware.app.util.ConnectionManager
import jb.openware.app.util.categoryUrl

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()
    private lateinit var connectionManager: ConnectionManager
    private lateinit var listView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectionManager = ConnectionManager()

        listView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = true
            setPaddingRelative(0, 0, 0, 0)
        }

        // Add RecyclerView into container from binding (layout1)
        binding.layout.addView(
            listView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // initial data load
        setData()
    }

    private fun setData() {
        connectionManager.startRequest("GET", categoryUrl, "A", object : ConnectionManager.RequestListener {
            override fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>) {

                val type = object : TypeToken<List<CategoryItem>>() {}.type
                val items: List<CategoryItem> = gson.fromJson(response, type)

                listView.adapter = CategoryAdapter(items)
            }

            override fun onErrorResponse(tag: String, message: String) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Alert")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        })
    }

    private inner class CategoryAdapter(private val data: List<CategoryItem>) :
        RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val cellBinding = CategoryCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(cellBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]

            holder.binding.t2.text = item.name

            holder.binding.i1.setColorFilter(holder.binding.root.context.getColorOnSurfaceVariant(), PorterDuff.Mode.SRC_IN)

            holder.binding.root.setOnClickListener {
                val intent = Intent(holder.binding.root.context, CategoryActivity::class.java).apply {
                    putExtra("code", "category")
                    putExtra("title", item.name)
                }
                startActivity(intent)
            }

            Glide.with(holder.binding.root.context)
                .load(item.url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(holder.binding.i1)
        }

        override fun getItemCount(): Int = data.size

        private fun Context.getColorOnSurfaceVariant(): Int {
            val typedValue = TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)
            return typedValue.data
        }

        inner class ViewHolder(val binding: CategoryCellBinding) : RecyclerView.ViewHolder(binding.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}