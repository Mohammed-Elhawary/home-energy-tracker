package com.micro.home_energy_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.micro.home_energy_tracker.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
