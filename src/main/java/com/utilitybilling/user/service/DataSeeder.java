package com.utilitybilling.user.service;

import com.utilitybilling.common.enums.RoleName;
import com.utilitybilling.user.entity.Role;
import com.utilitybilling.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            createRoleIfMissing(roleName);
        }
    }

    private void createRoleIfMissing(RoleName roleName) {
        if (roleRepository.existsByName(roleName)) {
            return;
        }

        Role role = new Role();
        role.setName(roleName);
        roleRepository.save(role);
    }
}
