package jb.openware.imageviewer.common.pager

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import jb.openware.imageviewer.common.extensions.forEach

internal abstract class RecyclingPagerAdapter<VH : RecyclingPagerAdapter.ViewHolder> :
    PagerAdapter() {

    companion object {
        private val STATE = RecyclingPagerAdapter::class.java.simpleName
        private const val VIEW_TYPE_IMAGE = 0
    }

    internal abstract fun getItemCount(): Int
    internal abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    internal abstract fun onBindViewHolder(holder: VH, position: Int)

    private val typeCaches = SparseArray<RecycleCache>()
    private var savedStates = SparseArray<Parcelable>()

    override fun destroyItem(parent: ViewGroup, position: Int, item: Any) {
        if (item is ViewHolder) {
            item.detach(parent)
        }
    }

    override fun getCount(): Int = getItemCount()

    override fun getItemPosition(item: Any): Int = POSITION_NONE

    @Suppress("UNCHECKED_CAST")
    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val cache = typeCaches[VIEW_TYPE_IMAGE] ?: RecycleCache(this).also {
            typeCaches.put(VIEW_TYPE_IMAGE, it)
        }

        return cache.getFreeViewHolder(parent, VIEW_TYPE_IMAGE).apply {
            attach(parent, position)
            onBindViewHolder(this as VH, position)
            onRestoreInstanceState(savedStates[getItemId(position)])
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean =
        obj is ViewHolder && obj.itemView === view

    override fun saveState(): Parcelable? {
        getAttachedViewHolders().forEach { viewHolder ->
            savedStates.put(
                getItemId(viewHolder.position),
                viewHolder.onSaveInstanceState()
            )
        }
        return Bundle().apply {
            putSparseParcelableArray(STATE, savedStates)
        }
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        if (state is Bundle) {
            state.classLoader = loader
            val sparseArray: SparseArray<Parcelable>? =
                state.getSparseParcelableArrayCompat(STATE)
            savedStates = sparseArray ?: SparseArray()
        }
        super.restoreState(state, loader)
    }

    private fun getItemId(position: Int): Int = position

    private fun getAttachedViewHolders(): List<ViewHolder> {
        val attachedViewHolders = ArrayList<ViewHolder>()

        // requires androidx.core:core-ktx for SparseArray.forEach
        typeCaches.forEach { _, value ->
            value.caches.forEach { holder ->
                if (holder.isAttached) {
                    attachedViewHolders.add(holder)
                }
            }
        }

        return attachedViewHolders
    }

    private class RecycleCache(
        private val adapter: RecyclingPagerAdapter<*>
    ) {

        val caches = mutableListOf<ViewHolder>()

        fun getFreeViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var index = 0
            while (index < caches.size) {
                val viewHolder = caches[index]
                if (!viewHolder.isAttached) {
                    return viewHolder
                }
                index++
            }

            return adapter.onCreateViewHolder(parent, viewType).also { caches.add(it) }
        }
    }

    internal abstract class ViewHolder(internal val itemView: View) {

        companion object {
            private val STATE = ViewHolder::class.java.simpleName
        }

        internal var position: Int = 0
        internal var isAttached: Boolean = false

        internal fun attach(parent: ViewGroup, position: Int) {
            isAttached = true
            this.position = position
            parent.addView(itemView)
        }

        internal fun detach(parent: ViewGroup) {
            parent.removeView(itemView)
            isAttached = false
        }

        internal fun onRestoreInstanceState(state: Parcelable?) {
            getStateFromParcelable(state)?.let { itemView.restoreHierarchyState(it) }
        }

        internal fun onSaveInstanceState(): Parcelable {
            val state = SparseArray<Parcelable>()
            itemView.saveHierarchyState(state)
            return Bundle().apply {
                putSparseParcelableArray(STATE, state)
            }
        }

        private fun getStateFromParcelable(state: Parcelable?): SparseArray<Parcelable>? {
            if (state is Bundle && state.containsKey(STATE)) {
                return state.getSparseParcelableArrayCompat(STATE)
            }
            return null
        }
    }
}

/**
 * Backward-compatible wrapper for [Bundle.getSparseParcelableArray] that
 * avoids the deprecation warning on Android 13+.
 */
private fun Bundle.getSparseParcelableArrayCompat(
    key: String
): SparseArray<Parcelable>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSparseParcelableArray(key, Parcelable::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSparseParcelableArray(key)
    }
}
