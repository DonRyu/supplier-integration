package com.donryu.supplier.feed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedController.class)
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedImportService importService;

    @Test
    void importsUploadedFeed() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "feed.csv", "text/csv", "item_no,desc,qty,unit_price\nA100,Milk,1,1.00".getBytes());
        when(importService.importFeed(eq("supplier-a"), eq(FeedFormat.CSV), any()))
                .thenReturn(new ImportResult("supplier-a", FeedFormat.CSV, 1, 0, 0, List.of()));

        mockMvc.perform(multipart("/api/feeds/supplier-a").file(file).param("format", "CSV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(1));
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "feed.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/feeds/supplier-a").file(file).param("format", "CSV"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Uploaded file is empty"));
    }

    @Test
    void rejectsUnknownFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "feed.json", "application/json", "{}".getBytes());

        mockMvc.perform(multipart("/api/feeds/supplier-a").file(file).param("format", "JSON"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestWhenFeedCannotBeParsed() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "feed.csv", "text/csv", "bad".getBytes());
        when(importService.importFeed(eq("supplier-a"), eq(FeedFormat.CSV), any()))
                .thenThrow(new FeedParseException("Missing CSV columns: qty"));

        mockMvc.perform(multipart("/api/feeds/supplier-a").file(file).param("format", "CSV"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Missing CSV columns: qty"));
    }
}
