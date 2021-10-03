package com.devsuperior.dscatalog.resources;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import javax.print.attribute.standard.Media;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO productDTO;
    private PageImpl<ProductDTO> page;
    private Long existingId;
    private Long nonExistingId;
    private Long dependentExistingId;

    @BeforeEach
    void setUp() throws Exception {
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of());

        existingId = 1L;
        nonExistingId = 2L;
        dependentExistingId = 3L;

        when(service.findAllPaged(any())).thenReturn(page);
        when(service.findById(existingId)).thenReturn(productDTO);
        when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        when(service.insert(any())).thenReturn(productDTO);

        when(service.update(eq(existingId), any())).thenReturn(productDTO);
        when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

        doNothing().when(service).delete(existingId);
        doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
        doThrow(DatabaseException.class).when(service).delete(dependentExistingId);

    }

    @Test
    public void insertShouldReturnProductDTO() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(
                post("/products")
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(
                put("/products/{id}", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());

    }

    @Test
    public void deleteShouldDeleteIdWhenExistsId() throws Exception {
        ResultActions result = mockMvc.perform(
                delete("/products/{id}", existingId)
                        .accept(MediaType.APPLICATION_JSON)

        );

        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldNotFoundWhenDoesNotExistsId() throws Exception {
        ResultActions result = mockMvc.perform(
                delete("/products/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON)

        );

        result.andExpect(status().isNotFound());
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(
                put("/products/{id}", nonExistingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isNotFound());

    }

    @Test
    public void findAllShouldReturnPage() throws Exception {
        ResultActions result = mockMvc.perform(
                get("/products").accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk());
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExist() throws Exception {
        ResultActions result = mockMvc.perform(
                get("/products/{id}", existingId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findByIdShouldThorwNotFoundWhenIdDoesNotExist() throws Exception {
        ResultActions result = mockMvc.perform(
                get("/products/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isNotFound());
    }
}
