package io.kgu.chatservice.controller;

import io.kgu.chatservice.domain.dto.user.UserDto;
import io.kgu.chatservice.domain.dto.user.RequestUserDto;
import io.kgu.chatservice.domain.dto.user.ResponseUserDto;
import io.kgu.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper mapper;

    // 회원 생성(UserDto.userId == null 인 경우)
    @PostMapping
    public ResponseUserDto createUser(@Valid @RequestBody RequestUserDto requestUserDto, HttpServletResponse resp) {

        UserDto userDto = userService.createUser(mapper.map(requestUserDto, UserDto.class));

        resp.setStatus(HttpServletResponse.SC_CREATED);
        return mapper.map(userDto, ResponseUserDto.class);
    }

    // auth, email 회원 조회
    @GetMapping
    public ResponseUserDto findUser(@RequestHeader("auth") String auth, @RequestHeader("email") String email) {

        UserDto userDto = userService.getUserByAuthAndEmail(auth, email);

        return mapper.map(userDto, ResponseUserDto.class);
    }

    // userId 회원 조회
    @GetMapping("/{userId}")
    public ResponseUserDto user(@PathVariable String userId) {

        UserDto userDto = userService.getUserByUserId(userId);

        return mapper.map(userDto, ResponseUserDto.class);
    }

    // 회원 정보 변경 요청
    @PutMapping("/update")
    public ResponseUserDto modifyUser(@RequestHeader String userId, @Valid @RequestBody RequestUserDto requestUserDto) {

        UserDto userDto = userService.modifyUserInfo(userId, mapper.map(requestUserDto, UserDto.class));

        return mapper.map(userDto, ResponseUserDto.class);
    }

    // 회원 프로필 사진 변경 요청
    @PutMapping("/update/image")
    public ResponseUserDto modifyUserPicture(@RequestHeader String userId, @RequestParam MultipartFile image) throws IOException {

        UserDto userDto = userService.modifyUserPicture(userId, image);

        return mapper.map(userDto, ResponseUserDto.class);
    }

    // 친구 목록 조회
    @GetMapping("/friends")
    public List<ResponseUserDto> friends(@RequestHeader("userId") String userId) {

        List<UserDto> friends = userService.getAllFriends(userId);

        return friends.stream()
                .map(u -> mapper.map(u, ResponseUserDto.class))
                .collect(Collectors.toList());
    }

    // 친구 단일 조회
    @GetMapping("/friends/{friendId}")
    public ResponseUserDto getOneFriends(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        UserDto friend = userService.getOneFriends(userId, friendId);

        return mapper.map(friend, ResponseUserDto.class);
    }

    // 친구 추가 요청
    @PatchMapping("/friends/{friendId}")
    public List<ResponseUserDto> addFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends = userService.addFriend(userId, friendId);

        return friends.stream()
                .map(u -> mapper.map(u, ResponseUserDto.class))
                .collect(Collectors.toList());
    }

    // 친구 삭제 요청
    @DeleteMapping("/friends/{friendId}")
    public List<ResponseUserDto> removeFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends = userService.removeFriend(userId, friendId);

        return friends.stream()
                .map(u -> mapper.map(u, ResponseUserDto.class))
                .collect(Collectors.toList());
    }

    // 차단 목록 조회
    @GetMapping("/blacklist")
    public List<ResponseUserDto> blocked(@RequestHeader("userId") String userId) {

        List<UserDto> blacklist = userService.getAllBlocked(userId);

        return blacklist.stream()
                .map(u -> mapper.map(u, ResponseUserDto.class))
                .collect(Collectors.toList());
    }

    // 차단 유저 단일 조회
    @GetMapping("/blacklist/{blockId}")
    public ResponseUserDto getOneBlocked(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        UserDto blocked = userService.getOneBlocked(userId, blockId);

        return mapper.map(blocked, ResponseUserDto.class);
    }

    // 유저 차단 요청
    @PatchMapping("/blacklist/{blockId}")
    public List<ResponseUserDto> blockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blacklist = userService.blockUser(userId, blockId);

        return blacklist.stream()
                .map(u -> mapper.map(u, ResponseUserDto.class))
                .collect(Collectors.toList());
    }

    // 유저 차단 해제 요청
    @DeleteMapping("/blacklist/{blockId}")
    public List<ResponseUserDto> unblockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blacklist = userService.unblockUser(userId, blockId);

        return blacklist.stream()
                .map(u -> mapper.map(u, ResponseUserDto.class))
                .collect(Collectors.toList());
    }

    // 회원 서비스 탈퇴
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") String userId) {

        userService.deleteUser(userId);
    }

}
