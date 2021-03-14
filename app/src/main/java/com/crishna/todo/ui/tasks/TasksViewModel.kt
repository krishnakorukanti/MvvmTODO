package com.crishna.todo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.crishna.todo.data.PreferencesManager
import com.crishna.todo.data.SortOrder
import com.crishna.todo.data.Task
import com.crishna.todo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,

    private val preferncesManager: PreferencesManager
) : ViewModel() {
    val searchQuery = MutableStateFlow("")

    val preferncesFlow = preferncesManager.preferencesFlow


    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery,
        preferncesFlow
    ) { query, filterPrefernces ->
        Pair(query, filterPrefernces)

    }

        .flatMapLatest { (query, filterPrefernces) ->
            taskDao.getTasks(query, filterPrefernces.sortOrder, filterPrefernces.hideCompleted)
        }
    val tasks = taskFlow.asLiveData()


    fun onSortOderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferncesManager.updateSortOder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferncesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) {}

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))

    }

    fun onUndoDeleteClicked(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    sealed class TasksEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
    }


}
