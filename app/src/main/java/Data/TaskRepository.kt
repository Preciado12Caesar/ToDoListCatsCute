package Data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    fun getCompletedTasks(): Flow<List<TaskEntity>> = taskDao.getCompletedTasks()

    suspend fun insertTask(description: String) {
        val task = TaskEntity(description = description)
        taskDao.insertTask(task)
    }

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        // CORREGIDO: Solo pasa el ID
        taskDao.updateTaskCompletion(task.id)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }
}