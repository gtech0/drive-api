package com.project.driveapi.repository;

import com.project.driveapi.entity.PathEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PathRepository extends JpaRepository<PathEntity, UUID> {
}
