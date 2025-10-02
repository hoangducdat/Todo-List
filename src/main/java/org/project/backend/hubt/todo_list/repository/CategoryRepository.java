package org.project.backend.hubt.todo_list.repository;

import org.project.backend.hubt.todo_list.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByNameAndUserId(String name, Long userId);
}