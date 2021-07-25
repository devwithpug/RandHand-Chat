package io.kgu.chatservice.controller;

import io.kgu.chatservice.domain.dto.UserDto;
import io.kgu.chatservice.domain.request.RequestUser;
import io.kgu.chatservice.domain.response.ResponseUser;
import io.kgu.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public ResponseEntity<ResponseUser> createUser(@Valid @RequestBody RequestUser requestUser) {

        UserDto userDto = mapper.map(requestUser, UserDto.class);

        userDto = userService.createUser(userDto);

        if (userDto == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

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
    public ResponseEntity<ResponseUser> modifyUser(@PathVariable String userId, @Valid @RequestBody RequestUser requestUser) {

        UserDto userDto = userService.modifyUserInfo(userId, mapper.map(requestUser, UserDto.class));

        if (userDto.getUserId() == null || userDto.getStatusMessage() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // 친구 목록 조회
    @GetMapping("/users/friends")
    public ResponseEntity<List<ResponseUser>> friends(@RequestHeader("userId") String userId) {

        List<UserDto> friends = userService.getAllFriends(userId);

        return getListResponseEntity(friends);
    }

    // 친구 단일 조회
    @GetMapping("/users/friends/{friendId}")
    public ResponseEntity<ResponseUser> getOneFriends(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        UserDto friend = userService.getOneFriends(userId, friendId);

        if (friend == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(friend, ResponseUser.class));
    }

    // 친구 추가 요청
    @PostMapping("/users/friends/{friendId}")
    public ResponseEntity<List<ResponseUser>> addFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends = userService.addFriend(userId, friendId);

        return getListResponseEntity(friends);
    }

    // 친구 삭제 요청
    @PostMapping("/users/friends/{friendId}/remove")
    public ResponseEntity<List<ResponseUser>> removeFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends = userService.removeFriend(userId, friendId);

        return getListResponseEntity(friends);
    }

    // 차단 목록 조회
    @GetMapping("/users/blocked")
    public ResponseEntity<List<ResponseUser>> blocked(@RequestHeader("userId") String userId) {

        List<UserDto> blockedList = userService.getAllBlocked(userId);

        return getListResponseEntity(blockedList);
    }

    // 차단 유저 단일 조회
    @GetMapping("/users/blocked/{blockId}")
    public ResponseEntity<ResponseUser> getOneBlocked(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        UserDto blocked = userService.getOneBlocked(userId, blockId);

        if (blocked == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(blocked, ResponseUser.class));
    }

    // 유저 차단 요청
    @PostMapping("/users/blocked/{blockId}")
    public ResponseEntity<List<ResponseUser>> blockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blockedList = userService.blockUser(userId, blockId);

        return getListResponseEntity(blockedList);
    }

    // 유저 차단 해제 요청
    @PostMapping("/users/blocked/{blockId}/remove")
    public ResponseEntity<List<ResponseUser>> unblockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blockedList = userService.unblockUser(userId, blockId);

        return getListResponseEntity(blockedList);
    }

    // 회원 서비스 탈퇴
    @GetMapping("/users/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("userId") String userId) {

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
