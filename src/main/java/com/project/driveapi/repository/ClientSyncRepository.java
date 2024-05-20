package com.project.driveapi.repository;

import com.project.driveapi.entity.ClientSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientSyncRepository extends JpaRepository<ClientSyncEntity, String> {
}
