package tr.org.lider.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import tr.org.lider.constant.RoleConstants;
import tr.org.lider.entities.ConkyTemplate;
import tr.org.lider.services.ConkyService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConkyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConkyService conkyService;

    @InjectMocks
    private ConkyController conkyController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(conkyController).build();
    }

    @Test
//    @Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_CONKY_DEFINITION, RoleConstants.ROLE_COMPUTERS })
    void conkyList() throws Exception {

        ConkyTemplate template = new ConkyTemplate();

        Page<ConkyTemplate> page = new PageImpl<>(Collections.singletonList(template), PageRequest.of(0, 10), 1);

        when(conkyService.list(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/conky/list/page-size/10/page-number/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(page)));
    }

    @Test
//    @Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_CONKY_DEFINITION, RoleConstants.ROLE_COMPUTERS })
    void conkyListAll() throws Exception {
        ConkyTemplate template = new ConkyTemplate();
        template.setId(1L);
        template.setLabel("Test Conky");
        template.setContents("Conky Content");

        List<ConkyTemplate> templates = Collections.singletonList(template);
        when(conkyService.listAll()).thenReturn(templates);

        mockMvc.perform(get("/api/conky/list-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(templates)));
    }

    @Test
//    @Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_CONKY_DEFINITION })
    void conkyAdd() throws Exception {
        ConkyTemplate inputTemplate = new ConkyTemplate();
        inputTemplate.setId(1L);
        inputTemplate.setLabel("New Conky");
        inputTemplate.setContents("New Content");

        when(conkyService.add(any(ConkyTemplate.class))).thenReturn(inputTemplate);

        mockMvc.perform(post("/api/conky/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputTemplate)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(inputTemplate)));
    }

    @Test
//    @Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_CONKY_DEFINITION })
    public void conkyUpdate() throws Exception {
        ConkyTemplate template = new ConkyTemplate();
        template.setId(1L);
        template.setLabel("Test Conky");
        template.setContents("Test Conky");

        when(conkyService.update(any(ConkyTemplate.class))).thenReturn(template);

        mockMvc.perform(post("/api/conky/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(template)));
    }

    @Test
//    @Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_CONKY_DEFINITION })
    void conkyDeleteById() throws Exception {
        Long id = 1L;
        ConkyTemplate deletedTemplate = new ConkyTemplate();
        deletedTemplate.setId(id);
        deletedTemplate.setLabel("To be deleted");
        deletedTemplate.setContents("Some contents");

        when(conkyService.delete(id)).thenReturn(deletedTemplate);

        mockMvc.perform(delete("/api/conky/delete/id/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(deletedTemplate)));
    }
}