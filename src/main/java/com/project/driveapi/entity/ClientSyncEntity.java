package com.project.driveapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "client_sync_entity")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSyncEntity {

    @Id
    private String clientId;

    @OneToMany(mappedBy = "sync")
    private List<PathEntity> paths;

}
