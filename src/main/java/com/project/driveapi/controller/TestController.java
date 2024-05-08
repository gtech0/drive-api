package com.project.driveapi.controller;

import org.springframework.web.bind.annotation.*;

/**
 * @author Yogesh Jadhav
 *
 */
@RestController
public class TestController {

//    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
//    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//
//    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,
//            "https://www.googleapis.com/auth/drive.install");
//
//    private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";
//
//    @Value("${google.oauth.callback.uri}")
//    private String CALLBACK_URI;
//
//    @Value("${google.secret.key.path}")
//    private Resource gdSecretKeys;
//
//    @Value("${google.credentials.folder.path}")
//    private Resource credentialsFolder;
//
//    @Value("${google.service.account.key}")
//    private Resource serviceAccountKey;
//
//    private GoogleAuthorizationCodeFlow flow;

//    @PostConstruct
//    public void init() throws Exception {
//        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY,
//                new InputStreamReader(gdSecretKeys.getInputStream()));
//        flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
//    }
//
//    @GetMapping(value = { "/" })
//    public String showHomePage() throws Exception {
//        boolean isUserAuthenticated = false;
//
//        Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
//        if (credential != null) {
//            boolean tokenValid = credential.refreshToken();
//            if (tokenValid) {
//                isUserAuthenticated = true;
//            }
//        }
//
//        return isUserAuthenticated ? "dashboard.html" : "index.html";
//    }
//
//    @GetMapping(value = "/sign-in")
//    public void doGoogleSignIn(HttpServletResponse response) throws Exception {
//        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
//        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
//        response.sendRedirect(redirectURL);
//    }
//
//    @GetMapping(value = "/oauth")
//    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
//        String code = request.getParameter("code");
//        if (code != null) {
//            saveToken(code);
//
//            return "dashboard.html";
//        }
//
//        return "index.html";
//    }
//
//    private void saveToken(String code) throws Exception {
//        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
//        flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
//    }
//
//    @GetMapping(value = "/create")
//    public void createFile(HttpServletResponse response) throws Exception {
//        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
//
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
//                .setApplicationName("googledrivespringbootexample").build();
//
//        File file = new File();
//        file.setName("profile.jpg");
//
//        FileContent content = new FileContent("image/jpeg", new java.io.File("D:\\practice\\sbtgd\\sample.jpg"));
//        File uploadedFile = drive.files().create(file, content).setFields("id").execute();
//
//        String fileReference = String.format("{fileID: '%s'}", uploadedFile.getId());
//        response.getWriter().write(fileReference);
//    }
//
//    @GetMapping(value = { "/uploadinfolder" })
//    public void uploadFileInFolder(HttpServletResponse response) throws Exception {
//        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
//
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
//                .setApplicationName("googledrivespringbootexample").build();
//
//        File file = new File();
//        file.setName("digit.jpg");
//        file.setParents(List.of("1_TsS7arQRBMY2t4NYKNdxta8Ty9r6wva"));
//
//        FileContent content = new FileContent("image/jpeg", new java.io.File("D:\\practice\\sbtgd\\digit.jpg"));
//        File uploadedFile = drive.files().create(file, content).setFields("id").execute();
//
//        String fileReference = String.format("{fileID: '%s'}", uploadedFile.getId());
//        response.getWriter().write(fileReference);
//    }
//
//    @GetMapping(value = { "/listfiles" }, produces = { "application/json" })
//    public @ResponseBody List<FileItemDTO> listFiles() throws Exception {
//        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
//
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
//                .setApplicationName("googledrivespringbootexample").build();
//
//        List<FileItemDTO> responseList = new ArrayList<>();
//
//        FileList fileList = drive.files().list().setFields("files(id,name,thumbnailLink)").execute();
//        for (File file : fileList.getFiles()) {
//            FileItemDTO item = new FileItemDTO();
//            item.setId(file.getId());
//            item.setName(file.getName());
//            item.setThumbnailLink(file.getThumbnailLink());
//            responseList.add(item);
//        }
//
//        return responseList;
//    }
//
//    @PostMapping(value = { "/makepublic/{fileId}" }, produces = { "application/json" })
//    private @ResponseBody Message makePublic(@PathVariable(name = "fileId") String fileId) throws Exception {
//        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
//
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
//                .setApplicationName("googledrivespringbootexample").build();
//
//        Permission permission = new Permission();
//        permission.setType("anyone");
//        permission.setRole("reader");
//
//        drive.permissions().create(fileId, permission).execute();
//
//        Message message = new Message();
//        message.setMessage("Permission has been successfully granted.");
//        return message;
//    }
//
//    @DeleteMapping(value = { "/deletefile/{fileId}" }, produces = "application/json")
//    private @ResponseBody Message deleteFile(@PathVariable(name = "fileId") String fileId) throws Exception {
//        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
//
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
//                .setApplicationName("googledrivespringbootexample").build();
//
//        drive.files().delete(fileId).execute();
//
//        Message message = new Message();
//        message.setMessage("File has been deleted.");
//        return message;
//    }
//
//    @GetMapping(value = { "/createfolder/{folderName}" }, produces = "application/json")
//    private @ResponseBody Message createFolder(@PathVariable(name = "folderName") String folder) throws Exception {
//        Credential cred = flow.loadCredential(USER_IDENTIFIER_KEY);
//
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred)
//                .setApplicationName("googledrivespringbootexample").build();
//
//        File file = new File();
//        file.setName(folder);
//        file.setMimeType("application/vnd.google-apps.folder");
//
//        drive.files().create(file).execute();
//
//        Message message = new Message();
//        message.setMessage("Folder has been created successfully.");
//        return message;
//    }
//
//    @GetMapping(value = { "/servicelistfiles" }, produces = { "application/json" })
//    private @ResponseBody List<FileItemDTO> listFilesInServiceAccount() throws Exception {
//        Credential cred = GoogleCredential.fromStream(serviceAccountKey.getInputStream());
//
//        GoogleClientRequestInitializer keyInitializer = new CommonGoogleClientRequestInitializer();
//
//        Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null).setHttpRequestInitializer(cred)
//                .setGoogleClientRequestInitializer(keyInitializer).build();
//
//        List<FileItemDTO> responseList = new ArrayList<>();
//
//        FileList fileList = drive.files().list().setFields("files(id,name,thumbnailLink)").execute();
//        for (File file : fileList.getFiles()) {
//            FileItemDTO item = new FileItemDTO();
//            item.setId(file.getId());
//            item.setName(file.getName());
//            item.setThumbnailLink(file.getThumbnailLink());
//            responseList.add(item);
//        }
//
//        return responseList;
//    }
//
//    @Setter
//    @Getter
//    private static class Message {
//        private String message;
//    }

}