package com.utilitybilling.user.service;

import com.utilitybilling.common.enums.RoleName;
import com.utilitybilling.user.entity.Role;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.RoleRepository;
import com.utilitybilling.user.repository.UserRepository;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.enabled:true}")
    private boolean seedAdminEnabled;

    @Value("${app.seed.admin.full-name:System Administrator}")
    private String adminFullName;

    @Value("${app.seed.admin.email:admin@utility.rw}")
    private String adminEmail;

    @Value("${app.seed.admin.phone-number:0788000000}")
    private String adminPhoneNumber;

    @Value("${app.seed.admin.password:password123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            createRoleIfMissing(roleName);
        }

        if (seedAdminEnabled) {
            createAdminIfMissing();
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

    private void createAdminIfMissing() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = new User();
        admin.setFullName(adminFullName);
        admin.setEmail(adminEmail);
        admin.setPhoneNumber(adminPhoneNumber);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(loadRoles(EnumSet.of(RoleName.ROLE_ADMIN)));
        userRepository.save(admin);
    }

    private Set<Role> loadRoles(Set<RoleName> roleNames) {
        return roleNames.stream()
            .map(roleName -> roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Missing role: " + roleName)))
            .collect(java.util.stream.Collectors.toSet());
    }
}
