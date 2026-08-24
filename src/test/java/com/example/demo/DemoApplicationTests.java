package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test /api/hello endpoint returns status UP and 200 OK")
    void testGetHelloEndpoint() throws Exception {
        mockMvc.perform(get("/api/hello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.message", containsString("Deployment Successful")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Test /api/greet/{name} endpoint returns personalized greeting")
    void testGreetUserEndpoint() throws Exception {
        String testName = "Saptarshi";

        mockMvc.perform(get("/api/greet/{name}", testName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.greeting", is("Hello, " + testName + "!")))
                .andExpect(jsonPath("$.deployedVia", is("Jenkins Docker Pipeline")));
    }
}