package com.michael.spring_boot_security.config;

import com.michael.spring_boot_security.entity.RoleEntity;
import com.michael.spring_boot_security.entity.base.RequestContext;
import com.michael.spring_boot_security.enumerations.Authority;
import com.michael.spring_boot_security.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    CommandLineRunner commandLineRunner(RoleRepository roleRepository) {
        return args -> {
            RequestContext.setUserId(0l);
            if (roleRepository.count() == 0) {
                var userRole = new RoleEntity();
                userRole.setName(Authority.USER.name());
                userRole.setAuthorities(Authority.USER);
                roleRepository.save(userRole);

                var adminRole = new RoleEntity();
                adminRole.setName(Authority.ADMIN.name());
                adminRole.setAuthorities(Authority.ADMIN);
                roleRepository.save(adminRole);

                var superAdminRole = new RoleEntity();
                userRole.setName(Authority.SUPER_ADMIN.name());
                userRole.setAuthorities(Authority.SUPER_ADMIN);
                roleRepository.save(superAdminRole);

                var managerRole = new RoleEntity();
                userRole.setName(Authority.MANAGER.name());
                userRole.setAuthorities(Authority.MANAGER);
                roleRepository.save(managerRole);
            }

            RequestContext.start();
        };
    }
}
