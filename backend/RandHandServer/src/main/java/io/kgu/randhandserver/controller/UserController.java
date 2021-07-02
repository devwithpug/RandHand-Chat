package io.kgu.randhandserver.controller;

import io.kgu.randhandserver.domain.dto.UserDto;
import io.kgu.randhandserver.domain.dto.UserInfoDto;
import io.kgu.randhandserver.domain.entity.User;
import io.kgu.randhandserver.security.config.LoginUser;
import io.kgu.randhandserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원 정보 조회
     */
    @GetMapping("/info")
    public UserInfoDto getUserInfo(@LoginUser User user) {

        return userService.getUserInfo(user);

    }

    /**
     * 회원 정보 변경 요청
     */
    @PostMapping("/info/modify")
    public UserInfoDto modifyUserInfo(@LoginUser User user, UserInfoDto info) {

        return userService.modifyUserInfo(user, info);

    }

    /**
     * 친구 목록 조회
     */
    @GetMapping("/friends")
    public List<UserDto> getFriends(@LoginUser User user) {

        return userService.getAllFriends(user);

    }

    /**
     * 친구 추가 요청
     */
    @PostMapping("/friends/add")
    public List<UserDto> addFriends(@LoginUser User user, Long targetId) {

        return userService.addFriend(user, targetId);

    }

    /**
     * 친구 삭제 요청
     */
    @PostMapping("/friends/remove")
    public List<UserDto> removeFriends(@LoginUser User user, Long targetId) {

        return userService.removeFriend(user, targetId);

    }

    /**
     * 차단 목록 조회
     */
    @GetMapping("/block")
    public List<UserDto> getBlock(@LoginUser User user) {

        return userService.getAllBlocked(user);

    }

    /**
     * 유저 차단 요청
     */
    @PostMapping("/block/add")
    public List<UserDto> addBlock(@LoginUser User user, Long targetId) {

        return userService.blockUser(user, targetId);

    }

    /**
     * 유저 차단 해제 요청
     */
    @PostMapping("/block/remove")
    public List<UserDto> removeBlock(@LoginUser User user, Long targetId) {

        return userService.unblockUser(user, targetId);

    }

    /**
     * 회원 서비스 탈퇴
     */
    @GetMapping("/delete")
    public Boolean deleteUser(@LoginUser User user) {

        return userService.deleteUser(user);

    }
}
