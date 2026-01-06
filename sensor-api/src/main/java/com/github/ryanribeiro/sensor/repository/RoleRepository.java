package com.github.ryanribeiro.sensor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.ryanribeiro.sensor.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{

    Object findByName(String string);
}
