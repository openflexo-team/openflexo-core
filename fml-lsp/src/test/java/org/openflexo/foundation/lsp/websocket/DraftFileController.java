package org.openflexo.foundation.lsp.websocket;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller that provides endpoints for listing files and retrieving file content.
 *
 * Base path: /api/files
 * 
 * This class is a draft for an API intended to serve FML files,
 * allowing them to be accessed and edited in a web editor.
 * 
 * The different files should be listed in the listFile() method.
 * 
 */
@RestController
@RequestMapping("/api/files")
public class DraftFileController {
	
	
	 /**
     * Lists available files.
     *
     * Currently returns a static list containing one sample file path.
     *
     * @return a list of maps, each containing file information (e.g., name)
     */
    @GetMapping
    public List<Map<String, String>> listFiles() {
        List<Map<String, String>> files = new ArrayList<>();
        
        //TODO give access to real files
        Map<String, String> file = new HashMap<>();
        file.put("name", "uri/of/the/file.fml");
        
        files.add(file);

        return files;
    }
    
    /**
     * Retrieves the content of a file specified by its URI.
     */
    @GetMapping("/content")
    public ResponseEntity<String> getFileContent(@RequestParam String uri) throws IOException {
    	// Get the path from the URI
        Path path = Paths.get(URI.create(uri));
        
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }

        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        return ResponseEntity.ok(content);
    }
}

