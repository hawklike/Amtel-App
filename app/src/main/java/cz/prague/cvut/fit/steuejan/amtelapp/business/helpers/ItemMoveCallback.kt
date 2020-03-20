package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class ItemMoveCallback(private val mAdapter: ItemTouchHelperContract) : ItemTouchHelper.Callback()
{
    override fun isLongPressDragEnabled(): Boolean  = true
    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int
    {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
    {
        mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    interface ItemTouchHelperContract
    {
        fun onItemMove(fromPosition: Int, toPosition: Int)
    }

}