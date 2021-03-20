package com.crishna.todo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.crishna.todo.R.color
import com.crishna.todo.data.Task
import com.crishna.todo.databinding.ItemTaskBinding

class TasksAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallBack()) {


    inner class TasksViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
                checkBoxComplete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClicked(task, checkBoxComplete.isChecked)
                    }
                }
            }
        }

        @SuppressLint("ResourceAsColor")
        fun bind(task: Task) {
            binding.apply {
                checkBoxComplete.isChecked = task.completed
                textItem.text = task.name
                textItem.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
                if (task.important){
                    cardId.setBackgroundColor(color.imp)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)

    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)

    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClicked(task: Task, isChecked: Boolean)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem


    }
}