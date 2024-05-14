package com.project.driveapi.service;

import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashService {

    private final CommonService commonService;

    public void trashFiles(List<String> files) throws Exception {
        for (String fileId : files) {
            File googleFile = new File();
            googleFile.setTrashed(true);
            commonService.getDrive()
                    .files()
                    .update(fileId, googleFile)
                    .execute();
        }
    }

    public void untrashFiles(List<String> files) throws Exception {
        for (String fileId : files) {
            File googleFile = new File();
            googleFile.setTrashed(false);
            commonService.getDrive()
                    .files()
                    .update(fileId, googleFile)
                    .execute();
        }
    }

    public void emptyTrash() throws Exception {
        commonService.getDrive()
                .files()
                .emptyTrash()
                .execute();
    }

}
