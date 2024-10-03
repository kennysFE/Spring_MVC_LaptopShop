package vn.hoidanit.laptopshop.service;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String handleHello() {
        return " Hello from Controllers";
    }

    public User handleSaveUser(User eric) {
        eric = this.userRepository.save(eric);
        System.out.println(eric.toString());
        return eric;
    }
}
