package com.telegram.SpringTelegram.db;

import com.telegram.SpringTelegram.model.User;
import com.telegram.SpringTelegram.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Component
@Service
public class UserDAO implements DataBaseOperation<User>{
    @Autowired
    UserRepository userRepository;

    public UserDAO(){}
    @Override
    public void save(User user){
        userRepository.save(user);
    }
    @Override
    public void deleteById(Long id){
        userRepository.deleteById(id);
    }
    @Override
    public void delete(User user){
        userRepository.delete(user);
    }
    @Override
    public Iterable<User> findAll(){
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
