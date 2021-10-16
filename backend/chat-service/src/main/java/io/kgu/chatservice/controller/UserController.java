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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseUser createUser(@Valid @RequestBody RequestUser requestUser, HttpServletResponse resp) {

        UserDto userDto = mapper.map(requestUser, UserDto.class);

        try {
            userDto = userService.createUser(userDto);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        resp.setStatus(HttpServletResponse.SC_CREATED);
        return mapper.map(userDto, ResponseUser.class);
    }

    // auth, email 회원 조회
    @GetMapping
    public ResponseUser findUser(@RequestHeader("auth") String auth, @RequestHeader("email") String email) {

        UserDto userDto;

        try {
            userDto = userService.getUserByAuthAndEmail(auth, email);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return mapper.map(userDto, ResponseUser.class);
    }

    // userId 회원 조회
    @GetMapping("/{userId}")
    public ResponseUser user(@PathVariable String userId) {

        UserDto userDto;

        try {
            userDto = userService.getUserByUserId(userId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return mapper.map(userDto, ResponseUser.class);
    }

    // 회원 정보 변경 요청
    @PutMapping("/update")
    public ResponseUser modifyUser(@RequestHeader String userId, @Valid @RequestBody RequestUser requestUser) {

        UserDto userDto;

        try {
            userDto = userService.modifyUserInfo(userId, mapper.map(requestUser, UserDto.class));
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return mapper.map(userDto, ResponseUser.class);
    }

    // 회원 프로필 사진 변경 요청
    @PutMapping("/update/image")
    public ResponseUser modifyUserPicture(@RequestHeader String userId, @RequestParam MultipartFile image) {

        UserDto userDto;

        try {
            userDto = userService.modifyUserPicture(userId, image);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return mapper.map(userDto, ResponseUser.class);
    }

    // 친구 목록 조회
    @GetMapping("/friends")
    public List<ResponseUser> friends(@RequestHeader("userId") String userId, HttpServletResponse resp) {

        List<UserDto> friends;

        try {
            friends = userService.getAllFriends(userId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return friends.stream()
                .map(u -> mapper.map(u, ResponseUser.class))
                .collect(Collectors.toList());
    }

    // 친구 단일 조회
    @GetMapping("/friends/{friendId}")
    public ResponseUser getOneFriends(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        UserDto friend;

        try {
            friend = userService.getOneFriends(userId, friendId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return mapper.map(friend, ResponseUser.class);
    }

    // 친구 추가 요청
    @PatchMapping("/friends/{friendId}")
    public List<ResponseUser> addFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends;

        try {
            friends = userService.addFriend(userId, friendId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return friends.stream()
                .map(u -> mapper.map(u, ResponseUser.class))
                .collect(Collectors.toList());
    }

    // 친구 삭제 요청
    @DeleteMapping("/friends/{friendId}")
    public List<ResponseUser> removeFriend(@RequestHeader("userId") String userId, @PathVariable String friendId) {

        List<UserDto> friends;

        try {
            friends = userService.removeFriend(userId, friendId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return friends.stream()
                .map(u -> mapper.map(u, ResponseUser.class))
                .collect(Collectors.toList());
    }

    // 차단 목록 조회
    @GetMapping("/blacklist")
    public List<ResponseUser> blocked(@RequestHeader("userId") String userId) {

        List<UserDto> blacklist;

        try {
            blacklist = userService.getAllBlocked(userId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return blacklist.stream()
                .map(u -> mapper.map(u, ResponseUser.class))
                .collect(Collectors.toList());
    }

    // 차단 유저 단일 조회
    @GetMapping("/blacklist/{blockId}")
    public ResponseUser getOneBlocked(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        UserDto blocked;

        try {
            blocked = userService.getOneBlocked(userId, blockId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        return mapper.map(blocked, ResponseUser.class);
    }

    // 유저 차단 요청
    @PatchMapping("/blacklist/{blockId}")
    public List<ResponseUser> blockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blacklist;

        try {
            blacklist = userService.blockUser(userId, blockId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return blacklist.stream()
                .map(u -> mapper.map(u, ResponseUser.class))
                .collect(Collectors.toList());
    }

    // 유저 차단 해제 요청
    @DeleteMapping("/blacklist/{blockId}")
    public List<ResponseUser> unblockUser(@RequestHeader("userId") String userId, @PathVariable String blockId) {

        List<UserDto> blacklist;

        try {
            blacklist = userService.unblockUser(userId, blockId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return blacklist.stream()
                .map(u -> mapper.map(u, ResponseUser.class))
                .collect(Collectors.toList());
    }

    // 회원 서비스 탈퇴
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") String userId) {

        try {
            userService.deleteUser(userId);
        } catch (UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

}
