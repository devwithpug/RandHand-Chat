package io.kgu.chatservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kgu.chatservice.domain.dto.UserDto;
import io.kgu.chatservice.domain.request.RequestUser;
import io.kgu.chatservice.domain.response.ResponseUser;
import io.kgu.chatservice.service.AmazonS3Service;
import io.kgu.chatservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@DisplayName("유저 컨트롤러 단위 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private UserServiceImpl userService;

    @MockBean
    private ModelMapper modelMapper;

    @MockBean
    private AmazonS3Service amazonS3Service;

    RequestUser requestUser = RequestUser.builder()
            .auth("google")
            .email("zm@gmail.com")
            .name("test")
            .picture("https://picture")
            .build();

    UserDto userDto = UserDto.builder()
            .auth("google")
            .userId("UUID")
            .email("zm@gmail.com")
            .name("test")
            .picture("https://picture")
            .statusMessage("")
            .build();

    ResponseUser responseUser = ResponseUser.builder()
            .userId("UUID")
            .email("zm@gmail.com")
            .name("test")
            .statusMessage("")
            .picture("https://picture")
            .build();

    UserDto friends1 = UserDto.builder()
            .userId("F1")
            .build();

    UserDto friends2 = UserDto.builder()
            .userId("F2")
            .build();

    ResponseUser resp1 = ResponseUser.builder()
            .userId("F1")
            .build();

    ResponseUser resp2 = ResponseUser.builder()
            .userId("F2")
            .build();

    @Test
    @DisplayName("회원 생성")
    void createUser() throws Exception {

        Mockito.when(modelMapper.map(requestUser, UserDto.class)).thenReturn(userDto);
        Mockito.when(userService.createUser(userDto)).thenReturn(userDto);
        Mockito.when(modelMapper.map(userDto, ResponseUser.class)).thenReturn(responseUser);

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("test")));

    }

    @Test
    @DisplayName("회원 조회")
    void user() throws Exception {

        Mockito.when(userService.getUserByUserId("UUID")).thenReturn(userDto);
        Mockito.when(modelMapper.map(userDto, ResponseUser.class)).thenReturn(responseUser);
        Mockito.when(amazonS3Service.upload(userDto.getPicture(), userDto.getUserId())).thenReturn("https://test.image");

        mvc.perform(get("/users/UUID"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("test")));

    }

    @Test
    @DisplayName("회원 정보 변경 요청")
    void modifyUser() throws Exception {

        requestUser.setUserId("UUID");
        requestUser.setName("changed");

        userDto.setName("changed");

        responseUser.setName("changed");

        Mockito.when(modelMapper.map(requestUser, UserDto.class)).thenReturn(userDto);
        Mockito.when(userService.modifyUserInfo("UUID", userDto)).thenReturn(userDto);
        Mockito.when(modelMapper.map(userDto, ResponseUser.class)).thenReturn(responseUser);

        mvc.perform(post("/users/update").header("userId", "UUID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("changed")));

    }

    @Test
    @DisplayName("친구 목록 조회")
    void getAllFriends() throws Exception {

        requestUser.setUserId("UUID");

        responseUser.setUserFriends(List.of(resp1, resp2));

        Mockito.when(userService.getAllFriends("UUID")).thenReturn(List.of(friends1, friends2));
        Mockito.when(modelMapper.map(friends1, ResponseUser.class)).thenReturn(resp1);
        Mockito.when(modelMapper.map(friends2, ResponseUser.class)).thenReturn(resp2);

        mvc.perform(get("/users/friends").header("userId", "UUID"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

    }

    @Test
    @DisplayName("친구 단일 조회")
    void getOneFriends() throws Exception {

        requestUser.setUserId("UUID");

        Mockito.when(userService.getOneFriends("UUID", "F1")).thenReturn(friends1);
        Mockito.when(modelMapper.map(friends1, ResponseUser.class)).thenReturn(resp1);

        mvc.perform(get("/users/friends/F1").header("userId", "UUID"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("F1")));

    }

    @Test
    @DisplayName("친구 추가 요청")
    void addFriends() throws Exception {

        requestUser.setUserId("UUID");

        Mockito.when(userService.addFriend("UUID", "F1")).thenReturn(List.of(friends1));
        Mockito.when(modelMapper.map(friends1, ResponseUser.class)).thenReturn(resp1);

        mvc.perform(post("/users/friends/F1").header("userId", "UUID"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

    }

    @Test
    @DisplayName("친구 삭제 요청")
    void removeFriends() throws Exception {

        requestUser.setUserId("UUID");

        responseUser.setUserFriends(List.of(resp1, resp2));

        Mockito.when(userService.removeFriend("UUID", "F1")).thenReturn(List.of(friends2));
        Mockito.when(modelMapper.map(friends2, ResponseUser.class)).thenReturn(resp2);

        mvc.perform(post("/users/friends/F1/remove").header("userId", "UUID"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

    }


    // 차단 서비스는 친구 서비스와 동일한 로직이므로 생략


    @Test
    @DisplayName("회원 서비스 탈퇴")
    void deleteUser() throws Exception {

        mvc.perform(get("/users/delete").header("userId", "UUID"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 요청 실패 테스트
     */

    @Test
    @DisplayName("회원 가입 실패")
    void createUserFail() throws Exception {

        Mockito.when(modelMapper.map(requestUser, UserDto.class)).thenReturn(userDto);

        doThrow(new DuplicateKeyException("createUserFailTest"))
                .when(userService).createUser(userDto);

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestUser)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.*").value("createUserFailTest"));

    }

    @Test
    @DisplayName("친구 추가 요청 실패")
    void addFriendsFail() throws Exception {

        doThrow(new IllegalArgumentException("addFriendsFailTest"))
                .when(userService).addFriend("UUID", "DuplicatedFriends");

        mvc.perform(post("/users/friends/DuplicatedFriends").header("userId", "UUID"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.*").value("addFriendsFailTest"));

    }

    @Test
    @DisplayName("회원 서비스 탈퇴 실패")
    void deleteUserFail() throws Exception {

        doThrow(new UsernameNotFoundException("deleteUserFailTest"))
                .when(userService).deleteUser("illegalUserId");

        mvc.perform(get("/users/delete").header("userId", "illegalUserId"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}