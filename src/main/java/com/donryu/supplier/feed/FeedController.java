package com.donryu.supplier.feed;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/feeds")
public class FeedController {

    private final FeedImportService importService;

    public FeedController(FeedImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value = "/{supplier}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult upload(
            @PathVariable String supplier,
            @RequestParam FeedFormat format,
            @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FeedParseException("Uploaded file is empty");
        }
        try (InputStream input = file.getInputStream()) {
            return importService.importFeed(supplier, format, input);
        }
    }
}
