package io.kgu.userservice.service;

import io.kgu.userservice.domain.dto.UserDto;
import io.kgu.userservice.domain.request.RequestUser;

import java.util.List;

public interface UserService {

    // 회원 생성(UserDto.userId == null 인 경우)
    UserDto createUser(UserDto userDto);
    // 회원 조회
    UserDto getUserByUserId(String userId);
    // 회원 정보 변경 요청(UserDto.userId != null 인 경우)
    UserDto modifyUserInfo(String userId, UserDto userDto);
    // 친구 목록 조회
    List<UserDto> getAllFriends(String userId);
    // 친구 단일 조회
    UserDto getOneFriends(String userId, String friendId);
    // 친구 추가 요청
    List<UserDto> addFriend(String userId, String friendId);
    // 친구 삭제 요청
    List<UserDto> removeFriend(String userId, String friendId);
    // 차단 목록 조회
    List<UserDto> getAllBlocked(String userId);
    // 차단 유저 단일 조회
    UserDto getOneBlocked(String userId, String blockId);
    // 유저 차단 요청
    List<UserDto> blockUser(String userId, String blockId);
    // 유저 차단 해제 요청
    List<UserDto> unblockUser(String userId, String blockId);
    // 회원 서비스 탈퇴
    void deleteUser(String userId);
    // 회원 검증
    public boolean validateUser(String... userId);

}
