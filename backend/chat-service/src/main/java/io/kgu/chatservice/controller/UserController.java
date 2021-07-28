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
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseUser.errorResponseDetails("회원가입 실패 : 이메일 중복"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(userDto, ResponseUser.class));
    }

    // 회원 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> user(@PathVariable String userId) {

        UserDto userDto = userService.getUserByUserId(userId);

        if (userDto == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseUser.errorResponseDetails("회원조회 실패 : 존재하지 않는 유저"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // 회원 정보 변경 요청(UserDto.userId != null 인 경우)
    @PostMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> modifyUser(@PathVariable String userId, @Valid @RequestBody RequestUser requestUser) {

        UserDto userDto = userService.modifyUserInfo(userId, mapper.map(requestUser, UserDto.class));

        if (userDto.getUserId() == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseUser.errorResponseDetails("회원정보변경 실패 : 존재하지 않는 유저"));
        } else if (userDto.getStatusMessage() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseUser.errorResponseDetails("회원정보변경 실패 : 상태 메세지 필요"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // 친구 목록 조회
    @GetMapping("/users/friends")
    public ResponseEntity<List<ResponseUser>> friends(@RequestHeader("userId") String userId) {

        List<UserDto> friends = userService.getAllFriends(userId);

        if (friends == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(List.of(ResponseUser.errorResponseDetails("친구목록조회 실패 : 존재하지 않는 유저입니다.")));
        }

        return getListResponseEntity(friends);
    }

    // 친구 단일 조회
    @GetMapping("/users/friends/{friendId}")
    public ResponseEntity<ResponseUser> getOneFriends(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        UserDto friend = userService.getOneFriends(userId, friendId);

        if (friend == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseUser.errorResponseDetails("친구조회 실패 : 회원 또는 친구가 존재하지 않거나 친구가 아닌경우 입니다."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(friend, ResponseUser.class));
    }

    // 친구 추가 요청
    @PostMapping("/users/friends/{friendId}")
    public ResponseEntity<List<ResponseUser>> addFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends = userService.addFriend(userId, friendId);

        if (friends == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(List.of(ResponseUser.errorResponseDetails("친구추가 실패 : 회원 또는 친구가 존재하지 않는 경우 입니다.")));
        }

        return getListResponseEntity(friends);
    }

    // 친구 삭제 요청
    @PostMapping("/users/friends/{friendId}/remove")
    public ResponseEntity<List<ResponseUser>> removeFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends = userService.removeFriend(userId, friendId);

        if (friends == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(List.of(ResponseUser.errorResponseDetails("친구삭제 실패 : 회원 또는 친구가 존재하지 않는 경우 입니다.")));
        }

        return getListResponseEntity(friends);
    }

    // 차단 목록 조회
    @GetMapping("/users/blocked")
    public ResponseEntity<List<ResponseUser>> blocked(@RequestHeader("userId") String userId) {

        List<UserDto> blockedList = userService.getAllBlocked(userId);

        if (blockedList == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(List.of(ResponseUser.errorResponseDetails("차단목록조회 실패 : 존재하지 않는 유저입니다.")));
        }

        return getListResponseEntity(blockedList);
    }

    // 차단 유저 단일 조회
    @GetMapping("/users/blocked/{blockId}")
    public ResponseEntity<ResponseUser> getOneBlocked(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        UserDto blocked = userService.getOneBlocked(userId, blockId);

        if (blocked == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseUser.errorResponseDetails("차단조회 실패 : 회원 또는 차단회원이 존재하지 않거나 차단하지 않은 경우 입니다."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(blocked, ResponseUser.class));
    }

    // 유저 차단 요청
    @PostMapping("/users/blocked/{blockId}")
    public ResponseEntity<List<ResponseUser>> blockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blockedList = userService.blockUser(userId, blockId);

        if (blockedList == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(List.of(ResponseUser.errorResponseDetails("유저차단 실패 : 회원 또는 차단할 유저가 존재하지 않는 경우 입니다.")));
        }

        return getListResponseEntity(blockedList);
    }

    // 유저 차단 해제 요청
    @PostMapping("/users/blocked/{blockId}/remove")
    public ResponseEntity<List<ResponseUser>> unblockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blockedList = userService.unblockUser(userId, blockId);

        if (blockedList == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(List.of(ResponseUser.errorResponseDetails("차단해제 실패 : 회원 또는 차단할 유저가 존재하지 않는 경우 입니다.")));
        }

        return getListResponseEntity(blockedList);
    }

    // 회원 서비스 탈퇴
    @GetMapping("/users/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("userId") String userId) {

        if (!userService.validateUser(userId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(List.of(ResponseUser.errorResponseDetails("회원탈퇴 실패 : 존재하지 않는 유저")));
        }

        userService.deleteUser(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUser.errorResponseDetails("회원탈퇴 성공 : " + userId));
    }

    private ResponseEntity<List<ResponseUser>> getListResponseEntity(List<UserDto> request) {

        if (request == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<ResponseUser> result = new ArrayList<>();

        request.forEach(f -> result.add(mapper.map(f, ResponseUser.class)));

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
