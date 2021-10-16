package io.kgu.chatservice.controller;

import io.kgu.chatservice.domain.dto.UserDto;
import io.kgu.chatservice.domain.request.RequestUser;
import io.kgu.chatservice.domain.response.ResponseUser;
import io.kgu.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper mapper;

    // 회원 생성(UserDto.userId == null 인 경우)
    @PostMapping
    public ResponseEntity<ResponseUser> createUser(@Valid @RequestBody RequestUser requestUser) {

        UserDto userDto = mapper.map(requestUser, UserDto.class);

        try {
            userDto = userService.createUser(userDto);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(userDto, ResponseUser.class));
    }

    // auth, email 회원 조회
    @GetMapping
    public ResponseEntity<ResponseUser> findUser(@RequestHeader("auth") String auth, @RequestHeader("email") String email) {

        UserDto userDto;

        try {
            userDto = userService.getUserByAuthAndEmail(auth, email);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // userId 회원 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseUser> user(@PathVariable String userId) {

        UserDto userDto;

        try {
            userDto = userService.getUserByUserId(userId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // 회원 정보 변경 요청
    @PutMapping("/update")
    public ResponseEntity<ResponseUser> modifyUser(@RequestHeader String userId, @Valid @RequestBody RequestUser requestUser) {

        UserDto userDto;

        try {
            userDto = userService.modifyUserInfo(userId, mapper.map(requestUser, UserDto.class));
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // 회원 프로필 사진 변경 요청
    @PutMapping("/update/image")
    public ResponseEntity<ResponseUser> modifyUserPicture(@RequestHeader String userId, @RequestParam MultipartFile image) {

        UserDto userDto;

        try {
            userDto = userService.modifyUserPicture(userId, image);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(userDto, ResponseUser.class));
    }

    // 친구 목록 조회
    @GetMapping("/friends")
    public ResponseEntity<List<ResponseUser>> friends(@RequestHeader("userId") String userId) {

        List<UserDto> friends;

        try {
            friends = userService.getAllFriends(userId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return getListResponseEntity(friends);
    }

    // 친구 단일 조회
    @GetMapping("/friends/{friendId}")
    public ResponseEntity<ResponseUser> getOneFriends(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        UserDto friend;

        try {
            friend = userService.getOneFriends(userId, friendId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(friend, ResponseUser.class));
    }

    // 친구 추가 요청
    @PatchMapping("/friends/{friendId}")
    public ResponseEntity<List<ResponseUser>> addFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends;

        try {
            friends = userService.addFriend(userId, friendId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return getListResponseEntity(friends);
    }

    // 친구 삭제 요청
    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<List<ResponseUser>> removeFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends;

        try {
            friends = userService.removeFriend(userId, friendId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return getListResponseEntity(friends);
    }

    // 차단 목록 조회
    @GetMapping("/blocked")
    public ResponseEntity<List<ResponseUser>> blocked(@RequestHeader("userId") String userId) {

        List<UserDto> blockedList;

        try {
            blockedList = userService.getAllBlocked(userId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return getListResponseEntity(blockedList);
    }

    // 차단 유저 단일 조회
    @GetMapping("/blacklist/{blockId}")
    public ResponseEntity<ResponseUser> getOneBlocked(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        UserDto blocked;

        try {
            blocked = userService.getOneBlocked(userId, blockId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(mapper.map(blocked, ResponseUser.class));
    }

    // 유저 차단 요청
    @PatchMapping("/blacklist/{blockId}")
    public ResponseEntity<List<ResponseUser>> blockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blockedList;

        try {
            blockedList = userService.blockUser(userId, blockId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return getListResponseEntity(blockedList);
    }

    // 유저 차단 해제 요청
    @DeleteMapping("/blacklist/{blockId}")
    public ResponseEntity<List<ResponseUser>> unblockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blockedList;

        try {
            blockedList = userService.unblockUser(userId, blockId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return getListResponseEntity(blockedList);
    }

    // 회원 서비스 탈퇴
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") String userId) {

        try {
            userService.deleteUser(userId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("deleted", userId));
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
