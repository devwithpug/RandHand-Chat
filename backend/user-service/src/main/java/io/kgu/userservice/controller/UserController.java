package io.kgu.userservice.controller;

import io.kgu.userservice.domain.dto.UserDto;
import io.kgu.userservice.domain.request.RequestUser;
import io.kgu.userservice.domain.response.ResponseUser;
import io.kgu.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper mapper;

    // 회원 생성(UserDto.userId == null 인 경우)
    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser requestUser) {

        UserDto userDto = mapper.map(requestUser, UserDto.class);

        userDto = userService.createUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(userDto, ResponseUser.class));
    }
    // 회원 조회
    // 회원 정보 변경 요청(UserDto.userId != null 인 경우)
    // 친구 목록 조회
    // 친구 추가 요청
    // 친구 삭제 요청
    // 차단 목록 조회
    // 유저 차단 요청
    // 유저 차단 해제 요청
    // 회원 서비스 탈퇴

}
