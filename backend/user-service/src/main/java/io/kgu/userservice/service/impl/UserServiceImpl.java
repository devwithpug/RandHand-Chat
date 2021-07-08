package io.kgu.userservice.service.impl;

import io.kgu.userservice.domain.dto.UserDto;
import io.kgu.userservice.domain.entity.UserEntity;
import io.kgu.userservice.repository.UserRepository;
import io.kgu.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Override
    public UserDto createUser(UserDto userDto) {

        if (userDto.getUserId() != null) {
            log.error("이미 존재하는 유저입니다. : {}", userDto.getUserId());
            return null;
        }

        userDto.setUserId(UUID.randomUUID().toString());
        userDto.setStatusMessage("");

        UserEntity userEntity = mapper.map(userDto, UserEntity.class);

        userRepository.save(userEntity);

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto modifyUserInfo(UserDto userDto) {

        if (!validateUser(userDto.getUserId())) return null;

        UserEntity userEntity = userRepository.findByUserId(userDto.getUserId());
        userEntity.update(userDto);
        userRepository.save(userEntity);

        return userDto;
    }

    @Override
    public List<UserDto> getAllFriends(String userId) {

        if (!validateUser(userId)) return null;

        UserEntity userEntity = userRepository.findByUserId(userId);

        return getFriendsList(userEntity);
    }

    @Override
    public List<UserDto> addFriend(String userId, String friendId) {

        if (!validateUser(userId, friendId)) return null;

        UserEntity userEntity = userRepository.findByUserId(userId);

        userEntity.addFriend(friendId);

        userRepository.save(userEntity);

        return getFriendsList(userEntity);
    }

    @Override
    public List<UserDto> removeFriend(String userId, String friendId) {

        if (!validateUser(userId, friendId)) return null;

        UserEntity userEntity = userRepository.findByUserId(userId);

        userEntity.removeFriend(friendId);

        userRepository.save(userEntity);

        return getFriendsList(userEntity);
    }

    @Override
    public List<UserDto> getAllBlocked(String userId) {

        if (!validateUser(userId)) return null;

        UserEntity userEntity = userRepository.findByUserId(userId);

        return getBlockedList(userEntity);
    }

    @Override
    public List<UserDto> blockUser(String userId, String blockId) {

        if (!validateUser(userId, blockId)) return null;

        UserEntity userEntity = userRepository.findByUserId(userId);

        userEntity.blockUser(blockId);

        userRepository.save(userEntity);

        return getBlockedList(userEntity);
    }

    @Override
    public List<UserDto> unblockUser(String userId, String blockId) {

        if (!validateUser(userId, blockId)) return null;

        UserEntity userEntity = userRepository.findByUserId(userId);

        userEntity.unblockUser(blockId);

        userRepository.save(userEntity);

        return getBlockedList(userEntity);
    }

    @Override
    public void deleteUser(String userId) {

        if (validateUser(userId)) {
            userRepository.deleteByUserId(userId);
        }

    }

    private boolean validateUser(String... userId) {

        boolean returnResult = true;

        for (String id : userId) {
            if (!userRepository.existsByUserId(id)) {
                log.error("존재하지 않는 유저입니다. : {}", id);
                returnResult = false;
            }
        }

        return returnResult;

    }

    private List<UserDto> getFriendsList(UserEntity userEntity) {

        List<UserDto> result = new ArrayList<>();

        userEntity.getUserFriends().forEach(f -> {
            UserEntity byUserId = userRepository.findByUserId(f);
            result.add(mapper.map(byUserId, UserDto.class));
        });

        return result;
    }

    private List<UserDto> getBlockedList(UserEntity userEntity) {

        List<UserDto> result = new ArrayList<>();

        userEntity.getUserBlocked().forEach(f -> {
            UserEntity byUserId = userRepository.findByUserId(f);
            result.add(mapper.map(byUserId, UserDto.class));
        });

        return result;
    }
}
