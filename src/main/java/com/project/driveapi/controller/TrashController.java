package com.project.driveapi.controller;

import com.project.driveapi.service.TrashService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

    @Operation(description = "Add files to trash")
    @PostMapping(value = "/files/trash")
    public void trashFiles(@RequestBody List<String> files) throws Exception {
        trashService.trashFiles(files);
    }

    @Operation(description = "Remove files from trash")
    @PostMapping(value = "/files/untrash")
    public void untrashFiles(@RequestBody List<String> files) throws Exception {
        trashService.untrashFiles(files);
    }

    @Operation(description = "Delete all files from trash")
    @DeleteMapping(value = "/files/trash")
    public void emptyTrash() throws Exception {
        trashService.emptyTrash();
    }

}
