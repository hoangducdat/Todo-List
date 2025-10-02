package org.project.backend.hubt.todo_list.repository;

import org.project.backend.hubt.todo_list.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Task> findByUserIdAndIsCompletedOrderByCreatedAtDesc(Long userId, Boolean isCompleted);

    List<Task> findByUserIdAndCategoryIdOrderByCreatedAtDesc(Long userId, Long categoryId);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND " +
           "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
           "(:isCompleted IS NULL OR t.isCompleted = :isCompleted) " +
           "ORDER BY t.createdAt DESC")
    List<Task> findTasksWithFilters(@Param("userId") Long userId,
                                  @Param("categoryId") Long categoryId,
                                  @Param("isCompleted") Boolean isCompleted);
}