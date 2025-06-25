package com.part4.team09.otboo.module.domain.user.repository;

import com.part4.team09.otboo.module.domain.user.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  
  boolean existsByEmail(String email);
}
