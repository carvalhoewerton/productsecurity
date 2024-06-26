package productsecurity.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import productsecurity.infra.TokenService;
import productsecurity.model.user.LoginResponseDto;
import productsecurity.model.user.RegisterDTO;
import productsecurity.model.user.User;
import productsecurity.repository.UserRepository;



@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    UserRepository repository;

    @Autowired
    TokenService tokenService;
    
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Validated RegisterDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated RegisterDTO data) {
        if (repository.findByLogin(data.login()) != null) {
            return ResponseEntity.badRequest().body("Login already exists");
        }

        String encodedPassword = passwordEncoder.encode(data.password());
        User newUser = new User(data.login(), encodedPassword, data.role());
        repository.save(newUser);

        return ResponseEntity.ok().body(newUser);
    }
}
