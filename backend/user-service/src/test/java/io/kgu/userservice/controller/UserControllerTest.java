package io.kgu.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.userservice.domain.request.RequestUser;
import io.kgu.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@WebMvcTest(controllers = UserController.class)
@MockBeans({
        @MockBean(UserService.class),
        @MockBean(ModelMapper.class)
})
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void createUser() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        RequestUser requestUser = RequestUser.builder()
                .auth("google")
                .email("zm@gmail.com")
                .name("test")
                .picture("picture")
                .build();

        mvc.perform(post("/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestUser)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

}