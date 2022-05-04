package nz.ac.canterbury.seng302.portfolio;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)

class TestingWebApplication {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetProject() throws Exception {
        this.mockMvc.perform(get("/login")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    void testAddSprint() throws Exception {
        this.mockMvc.perform(post("/portfolio/addSprint").param("projectId", "1")).andDo(print()).andExpect(status().isOk());
    }


}