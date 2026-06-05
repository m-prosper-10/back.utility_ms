package com.utilitybilling.user.repository;

import com.utilitybilling.common.enums.RoleName;
import com.utilitybilling.user.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByName(RoleName name);

    Optional<Role> findByName(RoleName name);
}
