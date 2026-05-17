package com.example.system1.repository;

import com.example.system1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByStudentId(String studentId);
    User findByEmail(String email);
}