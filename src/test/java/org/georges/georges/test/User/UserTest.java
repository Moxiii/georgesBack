package org.georges.georges.test.User;

import org.georges.georges.pojos.User;
import org.georges.georges.repository.UserRepository;
import org.georges.georges.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserTest {

    @Autowired
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testUserCreation() {
        // Données de test
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        // Création d'un nouvel utilisateur
        User user = new User("username", "email@example.com", rawPassword);

        // Mock de la méthode encode de passwordEncoder
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Appel de la méthode qui crée un nouvel utilisateur
        userService.createUser(user);

        // Vérification que la méthode encode de passwordEncoder a bien été appelée
        verify(passwordEncoder).encode(rawPassword);

        // Vérification que userRepository.save(user) a été appelé
        verify(userRepository).save(user);
    }

}