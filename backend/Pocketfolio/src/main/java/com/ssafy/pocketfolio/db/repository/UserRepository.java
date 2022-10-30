package com.ssafy.pocketfolio.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.pocketfolio.db.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	int countByEmail(String email);

}