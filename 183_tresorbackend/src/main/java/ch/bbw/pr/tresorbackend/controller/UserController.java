package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;
import ch.bbw.pr.tresorbackend.service.RecaptchaService;
import ch.bbw.pr.tresorbackend.service.UserService;

import ch.bbw.pr.tresorbackend.util.PasswordValidator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserController
 *
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private UserService userService;
    private PasswordEncryptionService passwordService;
    private final RecaptchaService recaptchaService;
    private final ConfigProperties configProperties;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(ConfigProperties configProperties, UserService userService,
                          PasswordEncryptionService passwordService, RecaptchaService recaptchaService) {
        this.configProperties = configProperties;
        this.recaptchaService = recaptchaService;
        System.out.println("UserController.UserController: cross origin: " + configProperties.getOrigin());
        // Logging in the constructor
        logger.info("UserController initialized: " + configProperties.getOrigin());
        logger.debug("UserController.UserController: Cross Origin Config: {}", configProperties.getOrigin());
        this.userService = userService;
        this.passwordService = passwordService;
    }

    // build create User REST API
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody RegisterUser registerUser, BindingResult bindingResult) {
        //captcha
        if (!recaptchaService.verifyCaptcha(registerUser.getCaptchaResponse())) {
            JsonObject err = new JsonObject();
            err.addProperty("message", "Captcha-Validierung fehlgeschlagen");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Gson().toJson(err));
        }

        System.out.println("UserController.createUser: captcha passed.");

        //input validation
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            System.out.println("UserController.createUser " + errors);

            JsonArray arr = new JsonArray();
            errors.forEach(arr::add);
            JsonObject obj = new JsonObject();
            obj.add("message", arr);
            String json = new Gson().toJson(obj);

            System.out.println("UserController.createUser, validation fails: " + json);
            return ResponseEntity.badRequest().body(json);
        }
        System.out.println("UserController.createUser: input validation passed");

            //password validation
        if (!PasswordValidator.isPasswordStrong(registerUser.getPassword())) {
            JsonObject err = new JsonObject();
            err.addProperty("message", "Password Validierung fehlgeschlagen");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Gson().toJson(err));
        }

        //transform registerUser to user
        User user = new User(
                null,
                registerUser.getFirstName(),
                registerUser.getLastName(),
                registerUser.getEmail(),
                passwordService.hashPassword(registerUser.getPassword())
        );

        User savedUser = userService.createUser(user);
        System.out.println("UserController.createUser, user saved in db");
        JsonObject obj = new JsonObject();
        obj.addProperty("answer", "User Saved");
        String json = new Gson().toJson(obj);
        System.out.println("UserController.createUser " + json);
        return ResponseEntity.accepted().body(json);
    }

    // build get user by id REST API
    // http://localhost:8080/api/users/1
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long userId) {
        User user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // Build Get All Users REST API
    // http://localhost:8080/api/users
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Build Update User REST API
    // http://localhost:8080/api/users/1
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long userId,
                                           @RequestBody User user) {
        user.setId(userId);
        User updatedUser = userService.updateUser(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // Build Delete User REST API
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>("User successfully deleted!", HttpStatus.OK);
    }


    // get user id by email
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping("/byemail")
    public ResponseEntity<String> getUserIdByEmail(@RequestBody EmailAdress email, BindingResult bindingResult) {
        System.out.println("UserController.getUserIdByEmail: " + email);
        //input validation
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            System.out.println("UserController.createUser " + errors);

            JsonArray arr = new JsonArray();
            errors.forEach(arr::add);
            JsonObject obj = new JsonObject();
            obj.add("message", arr);
            String json = new Gson().toJson(obj);

            System.out.println("UserController.createUser, validation fails: " + json);
            return ResponseEntity.badRequest().body(json);
        }

        System.out.println("UserController.getUserIdByEmail: input validation passed");

        User user = userService.findByEmail(email.getEmail());
        if (user == null) {
            System.out.println("UserController.getUserIdByEmail, no user found with email: " + email);
            JsonObject obj = new JsonObject();
            obj.addProperty("message", "No user found with this email");
            String json = new Gson().toJson(obj);

            System.out.println("UserController.getUserIdByEmail, fails: " + json);
            return ResponseEntity.badRequest().body(json);
        }
        System.out.println("UserController.getUserIdByEmail, user find by email");
        JsonObject obj = new JsonObject();
        obj.addProperty("answer", user.getId());
        String json = new Gson().toJson(obj);
        System.out.println("UserController.getUserIdByEmail " + json);
        return ResponseEntity.accepted().body(json);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping("/login")
    public ResponseEntity doLoginUser(@RequestBody LoginUser loginUser, BindingResult bindingResult) {
        System.out.println("UserController.doLoginUser: " + loginUser.getEmail());
        //input validation
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            System.out.println("UserController.doLoginUser " + errors);

            JsonArray arr = new JsonArray();
            errors.forEach(arr::add);
            JsonObject obj = new JsonObject();
            obj.add("message", arr);
            String json = new Gson().toJson(obj);

            System.out.println("UserController.doLoginUser, validation fails: " + json);
            return ResponseEntity.badRequest().body(json);
        }

        System.out.println("UserController.doLoginUser: input validation passed");

        User user = userService.findByEmail(loginUser.getEmail());
        if (user == null) {
            System.out.println("UserController.doLoginUser, no user found with email: " + loginUser.getEmail());
            JsonObject obj = new JsonObject();
            obj.addProperty("message", "No user found with this email");
            String json = new Gson().toJson(obj);

            System.out.println("UserController.doLoginUser, fails: " + json);
            return ResponseEntity.badRequest().body(json);
        }
        System.out.println("UserController.doLoginUser, user find by email");
        JsonObject obj = new JsonObject();
        obj.addProperty("answer", user.getId());
        String json = new Gson().toJson(obj);
        System.out.println("UserController.doLoginUser " + json);
        return ResponseEntity.accepted().body(json);
    }
}

