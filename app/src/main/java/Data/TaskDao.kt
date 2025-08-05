package Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao{
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    //obtener pendientes
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    //obtener completad
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    //actualizar
    @Update
    suspend fun updateTask(task: TaskEntity)

    //insertar
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    //eliminar
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // CORREGIDO: Solo usa taskID, quita isCompleted
    @Query("UPDATE tasks SET isCompleted = 1 WHERE id = :taskID")
    suspend fun updateTaskCompletion(taskID: Int)
}