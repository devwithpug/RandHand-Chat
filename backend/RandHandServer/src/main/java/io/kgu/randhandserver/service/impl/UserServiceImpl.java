package io.kgu.randhandserver.service.impl;

import io.kgu.randhandserver.domain.dto.UserDto;
import io.kgu.randhandserver.domain.dto.UserInfoDto;
import io.kgu.randhandserver.domain.entity.User;
import io.kgu.randhandserver.domain.entity.UserInfo;
import io.kgu.randhandserver.repository.UserRepository;
import io.kgu.randhandserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto dto(User user) {
        return UserDto.of(userRepository.findById(user.getId()).get());
    }

    @Override
    public UserInfoDto getUserInfo(User user) {

        return UserInfoDto.of(user);

    }

    @Override
    public UserInfoDto modifyUserInfo(User user, UserInfoDto info) {

        try {
            UserInfo prev = user.getUserInfo();
            prev.setStatusMessage(info.getStatusMessage());
            prev.setPicture(info.getPicture());
            user.setUserInfo(prev);
            userRepository.save(user);
            return info;
        } catch (Exception e) {
            throw new NoTransactionException("트랜잭션 실패");
        }

    }

    @Override
    public List<UserDto> getAllFriends(User user) {

        List<Long> friends = user.getUserFriends();

        friends.forEach(id -> {
            if (!userRepository.existsById(id)) friends.remove(id);
        });

        return friends.stream()
                .map(id -> UserDto.of(userRepository.findById(id).get()))
                .collect(Collectors.toList());

    }

    @Override
    public List<UserDto> addFriend(User user, Long friendId) {

        Optional<User> target = userRepository.findById(friendId);

        if (target.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 사용자 입니다.");
        }

        try {
            user.getUserFriends().add(friendId);
            userRepository.save(user);
            return getAllFriends(user);
        } catch (Exception e) {
            throw new NoTransactionException("트랜잭션 실패");
        }

    }

    @Override
    public List<UserDto> removeFriend(User user, Long friendId) {

        Optional<User> target = userRepository.findById(friendId);

        if (target.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 사용자 입니다.");
        } else if (!user.getUserFriends().contains(friendId)) {
            throw new NoSuchElementException("친구목록에 없는 사용자 입니다.");
        }

        try {
            user.getUserFriends().remove(friendId);
            userRepository.save(user);
            return getAllFriends(user);
        } catch (Exception e) {
            throw new NoTransactionException("트랜잭션 실패");
        }

    }

    @Override
    public List<UserDto> getAllBlocked(User user) {

        List<Long> blocked = user.getUserBlocked();

        blocked.forEach(id -> {
            if (!userRepository.existsById(id)) blocked.remove(id);
        });

        return blocked.stream()
                .map(id -> UserDto.of(userRepository.findById(id).get()))
                .collect(Collectors.toList());

    }

    @Override
    public List<UserDto> blockUser(User user, Long blockId) {

        Optional<User> target = userRepository.findById(blockId);

        if (target.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 사용자 입니다.");
        }

        try {
            user.getUserBlocked().add(blockId);
            userRepository.save(user);
            return getAllBlocked(user);
        } catch (Exception e) {
            throw new NoTransactionException("트랜잭션 실패");
        }

    }

    @Override
    public List<UserDto> unblockUser(User user, Long blockId) {

        Optional<User> target = userRepository.findById(blockId);

        if (target.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 사용자 입니다.");
        } else if (!user.getUserBlocked().contains(blockId)) {
            throw new NoSuchElementException("친구목록에 없는 사용자 입니다.");
        }

        try {
            user.getUserBlocked().remove(blockId);
            userRepository.save(user);
            return getAllBlocked(user);
        } catch (Exception e) {
            throw new NoTransactionException("트랜잭션 실패");
        }

    }

    @Override
    public boolean deleteUser(User user) {

        try {
            userRepository.delete(user);
            return true;
        } catch (Exception e) {
            throw new NoTransactionException("트랜잭션 실패");
        }

    }

}
