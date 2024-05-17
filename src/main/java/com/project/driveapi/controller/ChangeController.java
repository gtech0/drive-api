package com.project.driveapi.controller;

import com.project.driveapi.dto.RevisionDto;
import com.project.driveapi.service.ChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class ChangeController {

    private final ChangeService changeService;

    @GetMapping(value = "/files/{fileId}/revisions")
    public List<RevisionDto> listFileRevisions(@PathVariable String fileId) throws IOException {
        return changeService.listFileRevisions(fileId);
    }

}
