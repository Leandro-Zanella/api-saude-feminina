package api.saude.feminina.service.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Grava as mídias dos artigos em disco e devolve a URL de leitura. */
@Service
public class MediaService {

    private final Path uploadDir;

    public MediaService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath();
    }

    public String save(MultipartFile file) throws IOException {
        Files.createDirectories(uploadDir);
        var fileName = UUID.randomUUID() + this.getExtension(file.getOriginalFilename());
        file.transferTo(uploadDir.resolve(fileName));
        return "/media/" + fileName;
    }

    private String getExtension(String fileName) {
        var index = fileName.lastIndexOf('.');
        return index == -1 ? "" : fileName.substring(index);
    }
}
