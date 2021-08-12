package io.kgu.chatservice.service.impl;

import io.kgu.chatservice.domain.dto.UserDto;
import io.kgu.chatservice.domain.entity.UserEntity;
import io.kgu.chatservice.repository.UserRepository;
import io.kgu.chatservice.service.AmazonS3Service;
import io.kgu.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AmazonS3Service amazonS3Service;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Override
    public UserDto createUser(UserDto userDto) {

        if (userRepository.existsByAuthAndEmail(userDto.getAuth(), userDto.getEmail())) {
            throw new DuplicateKeyException(String.format(
                    "이미 존재하는 유저입니다 'auth: %s, email: %s'", userDto.getAuth(), userDto.getEmail()
            ));
        }

        userDto.setUserId(UUID.randomUUID().toString());
        userDto.setStatusMessage("");

        try {
            String image = amazonS3Service.upload(userDto.getPicture(), userDto.getUserId());
            userDto.setPicture(image);
        } catch (IOException e) {
            log.warn("IOException while uploading user profile image 'userId : {}'", userDto.getUserId());
        }

        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setUserFriends(new ArrayList<>());
        userEntity.setUserBlocked(new ArrayList<>());
        userRepository.save(userEntity);

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByAuthAndEmail(String auth, String email) {

        UserEntity userEntity = userRepository.findByAuthAndEmail(auth, email);

        if (userEntity == null) {
            throw new UsernameNotFoundException(String.format(
                    "존재하지 않는 유저입니다 'auth: %s, email: %s'", auth, email
            ));
        }

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {

        validateUserByUserId(userId);

        UserEntity userEntity = userRepository.findByUserId(userId);
        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            throw new UsernameNotFoundException(String.format(
                    "존재하지 않는 유저입니다 'email: %s'", email
            ));
        }

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto modifyUserInfo(String userId, UserDto userDto) {

        validateUserByUserId(userId);

        if (userDto.getStatusMessage() == null) {
            throw new IllegalArgumentException(
                    "상태 메세지가 반드시 필요합니다 'statusMessage : null'"
            );
        }

        UserEntity userEntity = userRepository.findByUserId(userDto.getUserId());
        userEntity.update(userDto);
        userRepository.save(userEntity);

        return userDto;
    }

    @Override
    public List<UserDto> getAllFriends(String userId) {

        validateUserByUserId(userId);

        UserEntity userEntity = userRepository.findByUserId(userId);

        return getFriendsList(userEntity);
    }

    @Override
    public UserDto getOneFriends(String userId, String friendId) {

        validateUserByUserId(userId,friendId);

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (!userEntity.getUserFriends().contains(friendId)) {
            throw new IllegalArgumentException(String.format(
                    "친구로 등록되지 않은 유저입니다 'userId: %s, friendId: %s'", userId, friendId
            ));
        }

        UserEntity friend = userRepository.findByUserId(friendId);

        return mapper.map(friend, UserDto.class);
    }

    @Override
    public List<UserDto> addFriend(String userId, String friendId) {

        validateUserByUserId(userId, friendId);

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity.getUserFriends().contains(friendId)) {
            throw new IllegalArgumentException(String.format(
                    "이미 친구로 등록된 유저입니다 'friendId: %s'", friendId
            ));
        }

        userEntity.getUserFriends().add(friendId);
        userRepository.save(userEntity);

        return getFriendsList(userEntity);
    }

    @Override
    public List<UserDto> removeFriend(String userId, String friendId) {

        validateUserByUserId(userId, friendId);

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (!userEntity.getUserFriends().contains(friendId)) {
            throw new IllegalArgumentException(String.format(
                    "친구로 등록되지 않은 유저입니다 'friendId: %s'", friendId
            ));
        }

        userEntity.getUserFriends().remove(friendId);
        userRepository.save(userEntity);

        return getFriendsList(userEntity);
    }

    @Override
    public List<UserDto> getAllBlocked(String userId) {

        validateUserByUserId(userId);

        UserEntity userEntity = userRepository.findByUserId(userId);

        return getBlockedList(userEntity);
    }

    @Override
    public UserDto getOneBlocked(String userId, String blockId) {

        validateUserByUserId(userId, blockId);

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (!userEntity.getUserBlocked().contains(blockId)) {
            throw new IllegalArgumentException(String.format(
                    "차단 목록에 존재하지 않는 유저입니다 'blockId : %s'", blockId
            ));
        }

        UserEntity blocked = userRepository.findByUserId(blockId);

        return mapper.map(blocked, UserDto.class);
    }

    @Override
    public List<UserDto> blockUser(String userId, String blockId) {

        validateUserByUserId(userId, blockId);

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity.getUserBlocked().contains(blockId)) {
            throw new IllegalArgumentException(String.format(
                    "이미 차단된 유저입니다 'blockId : %s'", blockId
            ));
        }

        userEntity.getUserBlocked().add(blockId);
        userRepository.save(userEntity);

        return getBlockedList(userEntity);
    }

    @Override
    public List<UserDto> unblockUser(String userId, String blockId) {

        validateUserByUserId(userId, blockId);

        UserEntity userEntity = userRepository.findByUserId(userId);

        if (!userEntity.getUserBlocked().contains(blockId)) {
            throw new IllegalArgumentException(String.format(
                    "차단된 유저가 아닙니다 'blockId : %s'", blockId
            ));
        }

        userEntity.getUserBlocked().remove(blockId);
        userRepository.save(userEntity);

        return getBlockedList(userEntity);
    }

    @Override
    public void deleteUser(String userId) {

        validateUserByUserId(userId);

        amazonS3Service.delete(userId);

        userRepository.deleteByUserId(userId);

    }

    @Override
    public void validateUserByUserId(String... userId) {

        for (String id : userId) {
            if (!userRepository.existsByUserId(id)) {
                throw new UsernameNotFoundException(String.format(
                        "존재하지 않는 유저입니다 'userId: %s'", id
                ));
            }
        }
    }

    private List<UserDto> getFriendsList(UserEntity userEntity) {

        List<UserDto> result = new ArrayList<>();

        Iterator<String> iter = userEntity.getUserFriends().iterator();

        return processIter(result, iter);
    }

    private List<UserDto> getBlockedList(UserEntity userEntity) {

        List<UserDto> result = new ArrayList<>();

        Iterator<String> iter = userEntity.getUserBlocked().iterator();

        return processIter(result, iter);
    }

    private List<UserDto> processIter(List<UserDto> result, Iterator<String> iter) {
        while (iter.hasNext()) {
            String id = iter.next();
            UserEntity byUserId = userRepository.findByUserId(id);

            if (byUserId == null) {
                iter.remove();
            } else {
                result.add(mapper.map(byUserId, UserDto.class));
            }
        }

        return result;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = userRepository.findByUserId(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("userId : " + username + " not exists!");
        }
        return new User(userEntity.getEmail(), userEntity.getUserId(),
                true, true, true, true,
                new ArrayList<>());

    }
}
