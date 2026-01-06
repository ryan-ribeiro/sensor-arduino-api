package com.github.ryanribeiro.sensor.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.github.ryanribeiro.sensor.domain.Role;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.repository.RoleRepository;
import com.github.ryanribeiro.sensor.repository.UserRepository;

import jakarta.transaction.Transactional;

@Configuration(proxyBeanMethods = false)
public class AdminUserConfig implements CommandLineRunner {
    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AdminUserConfig(
            RoleRepository roleRepository,
            UserRepository userRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        var roleAdmin = roleRepository.findByName(Role.Values.ROLE_ADMIN.name());

        var userAdmin = userRepository.findByUsername("admin");

        if (userAdmin.isPresent() && roleAdmin != null) {
            System.out.println("Admin já existe");
        } else {
            var user = new User();
            user.setUsername("admin");
            user.setPassword(bCryptPasswordEncoder.encode("admin"));
            user.setRoles(Set.of(roleAdmin != null ? (Role) roleAdmin : new Role(Role.Values.ROLE_ADMIN.name())));
            userRepository.save(user);
        }
    }
}
