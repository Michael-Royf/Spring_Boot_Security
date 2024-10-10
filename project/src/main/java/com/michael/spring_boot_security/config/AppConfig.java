package com.michael.spring_boot_security.config;

import com.michael.spring_boot_security.entity.RoleEntity;
import com.michael.spring_boot_security.entity.UserEntity;
import com.michael.spring_boot_security.entity.base.RequestContext;
import com.michael.spring_boot_security.enumerations.Authority;
import com.michael.spring_boot_security.repository.RoleRepository;
import com.michael.spring_boot_security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    CommandLineRunner commandLineRunner(RoleRepository roleRepository, UserRepository userRepository) {
        return args -> {
            RequestContext.setUserId(0l);
//            if (userRepository.count() == 0){
//                var user = UserEntity.builder()
//
//                        .build();
//            }


            if (roleRepository.count() == 0) {
                var userRole = new RoleEntity();
                userRole.setName(Authority.USER.name());
                userRole.setAuthorities(Authority.USER);
             //   userRole.setCreatedBy(0L);
                roleRepository.save(userRole);

                var adminRole = new RoleEntity();
                adminRole.setName(Authority.ADMIN.name());
                adminRole.setAuthorities(Authority.ADMIN);
              //  adminRole.setCreatedBy(0L);
                roleRepository.save(adminRole);

                var superAdminRole = new RoleEntity();
                superAdminRole.setName(Authority.SUPER_ADMIN.name());
                superAdminRole.setAuthorities(Authority.SUPER_ADMIN);
              //  superAdminRole.setCreatedBy(0L);
                roleRepository.save(superAdminRole);

                var managerRole = new RoleEntity();
                managerRole.setName(Authority.MANAGER.name());
                managerRole.setAuthorities(Authority.MANAGER);
              //  managerRole.setCreatedBy(0L);
                roleRepository.save(managerRole);
            }

            RequestContext.start();
        };
    }
}
