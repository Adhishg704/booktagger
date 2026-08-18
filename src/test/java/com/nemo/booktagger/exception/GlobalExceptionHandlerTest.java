package com.nemo.booktagger.exception;

import com.nemo.booktagger.rest.controller.JobController;
import com.nemo.booktagger.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @Test
    public void testResourceNotFoundExceptionReturns404ProblemDetail() throws Exception {
        when(jobService.getJob(999)).thenThrow(new ResourceNotFoundException("Job with id 999 has not been created"));

        mockMvc.perform(get("/api/v1/job/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Job with id 999 has not been created"))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    public void testUnexpectedExceptionReturns500WithGenericMessageOnly() throws Exception {
        when(jobService.getJob(1)).thenThrow(new RuntimeException("db connection refused at 10.0.0.5:5432"));

        mockMvc.perform(get("/api/v1/job/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }

    @Test
    public void testInvalidPathVariableTypeReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/job/not-a-number"))
                .andExpect(status().isBadRequest());
    }
}
