package com.project.driveapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "path")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathEntity {

    @Id
    private UUID id;

    @Column
    private String path;

    @ManyToOne
    @JoinColumn(name = "sync_id", nullable = false)
    private ClientSyncEntity sync;

}
