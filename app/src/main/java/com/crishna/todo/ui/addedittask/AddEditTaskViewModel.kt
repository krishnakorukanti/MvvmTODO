package com.crishna.todo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crishna.todo.data.Task
import com.crishna.todo.data.TaskDao
import com.crishna.todo.ui.ADD_TASK_RESULT_OK
import com.crishna.todo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }
    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            // invalid input message - snackbar
            showInvalidInputMsg("Name cannot be empty")
        }
        if (task != null) {
            val updateTask = task.copy(name = taskName, important = taskImportance)
            updatedTask(updateTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }

    }

    private fun showInvalidInputMsg(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))

    }

    private fun updatedTask(updateTask: Task) = viewModelScope.launch {
        taskDao.update(updateTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}