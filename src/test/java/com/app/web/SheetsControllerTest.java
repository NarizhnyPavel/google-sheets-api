package com.app.web;

import com.app.service.GoogleSheetsService;
import com.app.service.model.WriteResponse;
import com.app.service.web.SheetsApiController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.app.utils.ResponseBuilder.buildRequest;
import static com.app.utils.ResponseBuilder.buildResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SheetsApiController.class)
public class SheetsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoogleSheetsService googleSheetsService;

    private final Gson gson = new GsonBuilder().create();

    @Test
    void writeToGoogleSheets() throws Exception {
        final WriteResponse response = buildResponse();
        when(googleSheetsService.writeToTable(any())).thenReturn(response);

        mockMvc.perform(post("/api/sheets")
                .content(gson.toJson(buildRequest()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableUrl").value(response.getTableUrl()));
    }

}
