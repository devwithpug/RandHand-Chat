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

import java.util.ArrayList;
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

        if (userDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userDto = userService.createUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(userDto, ResponseUser.class));
    }

    // 회원 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> user(@PathVariable String userId) {

        UserDto userDto = userService.getUserByUserId(userId);

        if (userDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // 회원 정보 변경 요청(UserDto.userId != null 인 경우)
    @PostMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> modifyUser(@PathVariable String userId, @RequestBody RequestUser requestUser) {

        UserDto userDto = userService.modifyUserInfo(userId, mapper.map(requestUser, UserDto.class));

        if (userDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // 친구 목록 조회
    @GetMapping("/users/{userId}/friends")
    public ResponseEntity<List<ResponseUser>> friends(@PathVariable String userId) {

        List<UserDto> friends = userService.getAllFriends(userId);

        return getListResponseEntity(friends);
    }

    // 친구 추가 요청
    @PostMapping("/users/{userId}/friends/{friendId}")
    public ResponseEntity<List<ResponseUser>> addFriend(@PathVariable String userId, @PathVariable String friendId) {

        List<UserDto> friends = userService.addFriend(userId, friendId);

        return getListResponseEntity(friends);
    }

    // 친구 삭제 요청
    @PostMapping("/users/{userId}/friends/{friendId}/remove")
    public ResponseEntity<List<ResponseUser>> removeFriend(@PathVariable String userId, @PathVariable String friendId) {

        List<UserDto> friends = userService.removeFriend(userId, friendId);

        return getListResponseEntity(friends);
    }

    // 차단 목록 조회
    @GetMapping("/users/{userId}/blocked")
    public ResponseEntity<List<ResponseUser>> blocked(@PathVariable String userId) {

        List<UserDto> blockedList = userService.getAllBlocked(userId);

        return getListResponseEntity(blockedList);
    }

    // 유저 차단 요청
    @PostMapping("/users/{userId}/blocked/{blockId}")
    public ResponseEntity<List<ResponseUser>> blockUser(@PathVariable String userId, @PathVariable String blockId) {

        List<UserDto> blockedList = userService.blockUser(userId, blockId);

        return getListResponseEntity(blockedList);
    }

    // 유저 차단 해제 요청
    @PostMapping("/users/{userId}/blocked/{blockId}/remove")
    public ResponseEntity<List<ResponseUser>> unblockUser(@PathVariable String userId, @PathVariable String blockId) {

        List<UserDto> blockedList = userService.unblockUser(userId, blockId);

        return getListResponseEntity(blockedList);
    }

    // 회원 서비스 탈퇴
    @GetMapping("/users/{userId}/delete")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {

        if (!userService.validateUser(userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userService.deleteUser(userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<List<ResponseUser>> getListResponseEntity(List<UserDto> friends) {
        
        if (friends == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<ResponseUser> result = new ArrayList<>();

        friends.forEach(f -> result.add(mapper.map(f, ResponseUser.class)));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
