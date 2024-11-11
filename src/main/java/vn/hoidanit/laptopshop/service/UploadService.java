package vn.hoidanit.laptopshop.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletContext;

@Service
public class UploadService {

    private final ServletContext servletContext;

    public UploadService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {
        // don't upload files
        if (file.isEmpty())
            return "";

        // Absolute file path
        String rootPath = this.servletContext.getRealPath("/resources/images");
        String finalName = "";

        try {
            byte[] bytes = file.getBytes();

            // File directory (file path + / + name folder)
            File dir = new File(rootPath + File.separator + targetFolder);
            if (!dir.exists())
                dir.mkdirs();

            // Create the file on server

            // Name file
            finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();

            File serverFile = new File(dir.getAbsolutePath() + File.separator + finalName);

            BufferedOutputStream stream = new BufferedOutputStream(
                    new FileOutputStream(serverFile));
            stream.write(bytes);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return finalName;
    }

    // Delete file from root path
    public void deleteAllImages() throws IOException {

        String pathTemporaryFolder = this.servletContext.getRealPath("/resources/images/temporary");

        File directory = new File(pathTemporaryFolder);

        // Kiểm tra nếu thư mục tồn tại
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles(); // Lấy danh sách tất cả tệp trong thư mục

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean isDeleted = file.delete(); // Xóa từng tệp
                        if (!isDeleted) {
                            throw new IOException("file can not delete " + file.getName());
                        }
                    }
                }
            }
        } else {
            throw new IOException(" Directory not found " + pathTemporaryFolder);
        }
    }

    // Destination file from temporary folder => format folder
    public boolean moveFile(String sourcePath, String destinationPath) {
        Path source = Paths.get(sourcePath).toAbsolutePath();
        Path destination = Paths.get(destinationPath).toAbsolutePath();

        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Tệp đã được di chuyển thành công từ " + sourcePath + " đến " + destinationPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi khi di chuyển tệp: " + e.getMessage());
            return false;
        }
    }
}
