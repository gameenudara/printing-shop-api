package lk.oracene.hardware_management_api;

import lk.oracene.hardware_management_api.model.RoleType;
import lk.oracene.hardware_management_api.model.User;
import lk.oracene.hardware_management_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class HardwareManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HardwareManagementApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByEmail("admin@hardware.lk")) {
				User admin = new User();
				admin.setFirstName("Admin");
				admin.setLastName("User");
				admin.setEmail("admin@hardware.lk");
				admin.setPassword(passwordEncoder.encode("Admin@1234"));
				admin.setRole(RoleType.ADMIN);
				admin.setIsActive(true);
				userRepository.save(admin);
				System.out.println("Default admin user created — email: admin@hardware.lk  password: Admin@1234");
			}
		};
	}
}
