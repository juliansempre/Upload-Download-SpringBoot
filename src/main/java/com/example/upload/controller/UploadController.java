package com.example.upload.controller;

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

@RestController
public class UploadController {

    private static final String UPLOAD_DIR = "static/uploads";

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
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/download/").path(fileName).toUriString();

            return ResponseEntity.ok("<!--Imagem enviada com sucesso! URL de download: " + downloadUrl + "--> <script>" + "alert('Arquivo enviado com sucesso!');" + " window.location.href = '/';" + "</script>");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("" + "<script>" + "alert('Erro ao enviar!');" + " window.location.href = '/';" + "</script>");
        }
    }

    @GetMapping("/downloads")
    public ResponseEntity<String> listDownloads() {
        try {
            List<String> downloadLinks = Files.list(Path.of(UPLOAD_DIR)).map(file -> {
                String fileName = file.getFileName().toString();
                return "<img src='uploads/" + fileName + "'style='width: 80px;'><br>" + "<a href='/download/" + fileName + "' download> Clique para baixar: " + fileName + "</a><br>";
            }).collect(Collectors.toList());

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

    //Exibir as imagens
    @GetMapping("/image/{imageName}")
    public ResponseEntity<String> getImage(@PathVariable String imageName) {
        // O caminho para a pasta onde as imagens estão armazenadas
        String imagePath = "/uploads/";

        // Construir a tag HTML <img> com o caminho da imagem
        String imageTag = "<img src='" + imagePath + imageName + "'>";

        // Retornar a tag HTML no corpo da resposta
        return ResponseEntity.ok().contentType(org.springframework.http.MediaType.TEXT_HTML).body(imageTag);
    }

    @GetMapping("/downloads/api")
    public ResponseEntity<Map<String, List<String>>> listDownloadsAPI() {
        try {
            List<String> fileNames = Files.list(Path.of(UPLOAD_DIR)).map(file -> file.getFileName().toString()).collect(Collectors.toList());

            Map<String, List<String>> response = new HashMap<>();
            response.put("meusarquivos", fileNames);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
