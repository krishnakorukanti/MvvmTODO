package com.crishna.todo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.crishna.todo.data.PreferencesManager
import com.crishna.todo.data.SortOrder
import com.crishna.todo.data.Task
import com.crishna.todo.data.TaskDao
import com.crishna.todo.ui.ADD_TASK_RESULT_OK
import com.crishna.todo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,

    private val preferncesManager: PreferencesManager,

    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    val searchQuery = state.getLiveData("searchQuery", "")

    val preferncesFlow = preferncesManager.preferencesFlow


    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery.asFlow(),
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

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

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

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmation("Task Added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmation("Task Updated")


        }

    }

    private fun showTaskSavedConfirmation(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMsg(text))

    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMsg(val msg: String) : TasksEvent()
    }


}
