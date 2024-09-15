import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskData)

    @Delete
    suspend fun deleteTask(task: TaskData)

    @Update
    suspend fun updateTask(task: TaskData)
}
