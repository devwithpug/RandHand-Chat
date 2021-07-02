package io.kgu.randhandserver.service;

import io.kgu.randhandserver.domain.dto.UserDto;
import io.kgu.randhandserver.domain.dto.UserInfoDto;
import io.kgu.randhandserver.domain.entity.User;

import java.util.List;

public interface UserService {

    // 회원 정보 조회
    UserInfoDto getUserInfo(User user);
    // 회원 정보 변경 요청
    UserInfoDto modifyUserInfo(User user, UserInfoDto info);
    // 친구 목록 조회
    List<UserDto> getAllFriends(User user);
    // 친구 추가 요청
    List<UserDto> addFriend(User user, Long friendId);
    // 친구 삭제 요청
    List<UserDto> removeFriend(User user, Long friendId);
    // 차단 목록 조회
    List<UserDto> getAllBlocked(User user);
    // 유저 차단 요청
    List<UserDto> blockUser(User user, Long blockId);
    // 유저 차단 해제 요청
    List<UserDto> unblockUser(User user, Long blockId);
    // 회원 서비스 탈퇴
    boolean deleteUser(User user);

}
