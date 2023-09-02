package com.example.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class ImageUploadDownloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageUploadDownloadApplication.class, args);
    }
}

@RestController
class ImageController {

    private static final String UPLOAD_DIR = "./uploads";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("imagem") MultipartFile file) {
        try {
            // Verifique se o diretório de upload existe, se não, crie-o
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Gere um nome de arquivo único usando a data atual e o nome original do arquivo
            String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            Path filePath = Path.of(UPLOAD_DIR, fileName);

            // Salve o arquivo no servidor
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Crie a URL de download para o arquivo recém-carregado
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/download/")
                    .path(fileName)
                    .toUriString();

            return ResponseEntity.ok("Imagem enviada com sucesso! URL de download: " + downloadUrl +
                    "<script>" +
                    "alert('Arquivo enviado com sucesso!');" +
                    " window.location.href = '/';" +
                    "</script>"
            );
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao fazer upload da imagem."+
                    "<script>" +
                    "alert('Arquivo enviado com sucesso!');" +
                    " window.location.href = '/';" +
                    "</script>"
            );
        }
    }

    @GetMapping("/downloads")
    public ResponseEntity<String> listDownloads() {
        try {
            List<String> downloadLinks = Files.list(Path.of(UPLOAD_DIR))
                    .map(file -> {
                        String fileName = file.getFileName().toString();
                        return "<a href='/download/" + fileName + "' download>" + fileName + "</a><br>";
                    })
                    .collect(Collectors.toList());

            StringBuilder htmlResponse = new StringBuilder("<html><body>");
            downloadLinks.forEach(link -> htmlResponse.append(link));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "text/html");

            htmlResponse.append("</body></html>");

            return new ResponseEntity<>(htmlResponse.toString(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao listar arquivos.");
        }
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Path.of(UPLOAD_DIR, filename);
            byte[] fileContent = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + filename);
            headers.add("Content-Type", "application/octet-stream");

            return ResponseEntity.ok().headers(headers).body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/downloads/api")
    public ResponseEntity<Map<String, List<String>>> listDownloadsAPI() {
        try {
            List<String> fileNames = Files.list(Path.of(UPLOAD_DIR))
                    .map(file -> file.getFileName().toString())
                    .collect(Collectors.toList());

            Map<String, List<String>> response = new HashMap<>();
            response.put("meusarquivos", fileNames);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
