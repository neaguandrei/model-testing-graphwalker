package com.fmi.user.repository;

import com.fmi.user.dao.entity.UserEntity;
import com.fmi.user.dao.repository.UserRepository;
import com.fmi.user.exception.NotFoundException;
import org.graphwalker.core.condition.TimeDuration;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.fmi.user.util.TestDataUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@RunWith(SpringRunner.class)
public class UserRepositoryTest extends ExecutionContext {

    @Autowired
    private UserRepository userRepository;

    private static PasswordEncoder passwordEncoder;

    private static UserEntity user;

    private String expectedPassword;

    private Integer notesCounter;

    private boolean isActive;

    @BeforeClass
    public static void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
        user = createUser();
    }

    @Test
    public void testUserRepositoryStateFlow() {
        final Vertex notExisting = new Vertex().setName("v_notExisting");
        final Vertex existing = new Vertex().setName("v_existing");
        final Vertex active = new Vertex().setName("v_active");

        final Model model = createModel(notExisting, existing, active);
        setModel(model.build());
        setPathGenerator(new RandomPath(new TimeDuration(10, TimeUnit.SECONDS)));
        setNextElement(notExisting);

        final Machine machine = new SimpleMachine(this);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }

        assertFalse(machine.hasNextStep());
    }

    public void v_notExisting() {
        assertFalse(userRepository.findByEmail(user.getEmail()).isPresent());
    }

    public void v_existing() {
        final UserEntity userEntity = userRepository.findByEmail(user.getEmail()).orElse(null);
        assertAll(
                () -> assertNotNull(userEntity),
                () -> assertFalse(Objects.requireNonNull(userEntity).isEnabled())
        );
    }

    public void v_active() {
        final UserEntity userEntity = userRepository.findByEmail(user.getEmail()).orElse(null);
        assertAll(
                () -> assertNotNull(userEntity),
                () -> assertTrue(passwordEncoder.matches(expectedPassword, Objects.requireNonNull(userEntity).getPassword())),
                () -> assertTrue(Objects.requireNonNull(userEntity).isEnabled()),
                () -> assertEquals(notesCounter, Objects.requireNonNull(userEntity).getNotes().size())
        );
    }

    public void e_registerUser() {
        final UserEntity userEntity = createUser();

        expectedPassword = userEntity.getPassword();
        isActive = userEntity.isEnabled();
        notesCounter = userEntity.getNotes().size();

        final String encryptedPassword = passwordEncoder.encode(expectedPassword);
        userEntity.setPassword(encryptedPassword);
        userRepository.save(userEntity);
    }

    public void e_deleteUser() {
        userRepository.deleteByEmail(user.getEmail());
    }

    public void e_enableUser() throws NotFoundException {
        final UserEntity existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundException("Not found"));

        existingUser.setEnabled(true);
        userRepository.save(existingUser);
    }

    public void e_disableUser() throws NotFoundException {
        final UserEntity existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundException("Not found"));

        existingUser.setEnabled(false);
        userRepository.save(existingUser);
    }

    public void e_updatePassword() throws NotFoundException {
        final UserEntity existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundException("Not found"));

        expectedPassword = generateRandomPassword();
        existingUser.setPassword(passwordEncoder.encode(expectedPassword));
        userRepository.save(existingUser);
    }

    public void e_addNote() throws NotFoundException {
        final UserEntity existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundException("Not found"));

        existingUser.getNotes().add(createNote());
        userRepository.save(existingUser);
        notesCounter++;
    }

    public void e_deleteNote() throws NotFoundException {
        final UserEntity existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundException("Not found"));

        existingUser.getNotes().remove(0);
        userRepository.save(existingUser);
        notesCounter--;
    }

    private Model createModel(Vertex notExisting, Vertex existing, Vertex active) {
        return new Model()
                .addEdge(new Edge().setName("e_registerUser")
                        .addAction(new Action("var password = '" + expectedPassword + "'"))
                        .addAction(new Action("var isActive = false"))
                        .addAction(new Action("var notesCounter = 0"))
                        .setSourceVertex(notExisting)
                        .setTargetVertex(existing))
                .addEdge(new Edge().setName("e_deleteUser")
                        .setSourceVertex(existing)
                        .setTargetVertex(notExisting))
                .addEdge(new Edge().setName("e_enableUser")
                        .setGuard(new Guard("isActive === false"))
                        .addAction(new Action("isActive = '" + !isActive + "'"))
                        .setSourceVertex(existing)
                        .setTargetVertex(active))
                .addEdge(new Edge().setName("e_disableUser")
                        .setGuard(new Guard("isActive === true"))
                        .addAction(new Action("isActive = '" + !isActive + "'"))
                        .setSourceVertex(active)
                        .setTargetVertex(existing))
                .addEdge(new Edge().setName("e_addNote")
                        .addAction(new Action("notesCounter++"))
                        .setSourceVertex(active)
                        .setTargetVertex(active))
                .addEdge(new Edge().setName("e_deleteNote")
                        .setGuard(new Guard("notesCounter >= 1"))
                        .addAction(new Action("notesCounter--"))
                        .setSourceVertex(active)
                        .setTargetVertex(active))
                .addEdge(new Edge().setName("e_updatePassword")
                        .setGuard(new Guard("password != null"))
                        .addAction(new Action("password = '" + expectedPassword + "'"))
                        .setSourceVertex(active)
                        .setTargetVertex(active));
    }
}
