package com.crishna.todo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crishna.todo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDataBase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        private val dataBase: Provider<TaskDataBase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

           val dao = dataBase.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Wash The Dishes"))
                dao.insert(Task("Do Laundry",completed = true))
                dao.insert(Task("Buy Groceries"))
                dao.insert(Task("Prepare Food",important = true))
                dao.insert(Task("Pay the dues",completed = true))
                dao.insert(Task("Call Elon Musk",important = true))
            }
        }
    }
}