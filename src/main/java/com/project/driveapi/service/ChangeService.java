package com.project.driveapi.service;

import com.google.api.services.drive.model.Revision;
import com.project.driveapi.dto.RevisionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeService {

    private final CommonService commonService;

    public List<RevisionDto> listFileRevisions(String fileId) throws IOException {
        List<Revision> revisions = commonService.getDrive()
                .revisions()
                .list(fileId)
                .setFields("revisions(id,lastModifyingUser,mimeType,modifiedTime,originalFilename,size)")
                .execute()
                .getRevisions();

        List<RevisionDto> revisionsDto = new ArrayList<>();
        for (Revision revision : revisions) {
            revisionsDto.add(RevisionDto.builder()
                    .id(revision.getId())
                    .lastModifyingUser(revision.getLastModifyingUser())
                    .mimeType(revision.getMimeType())
                    .modifiedTime(commonService.unixToLocalDateTime(revision.getModifiedTime().getValue()))
                    .originalFileName(revision.getOriginalFilename())
                    .size(revision.getSize())
                    .build());
        }

        return revisionsDto;
    }
}
