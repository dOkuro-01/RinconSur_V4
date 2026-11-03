package cl.duoc.rinconsur;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventoControllerTest {
    @Autowired MockMvc mvc;
    @Test void saludResponde() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("OK"));
    }
    @Test void registrarEvento() throws Exception {
        mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON)
             .content("{\"tipo\":\"TEST\",\"detalle\":\"Ejemplo\"}"))
             .andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("TEST"));
    }
}
