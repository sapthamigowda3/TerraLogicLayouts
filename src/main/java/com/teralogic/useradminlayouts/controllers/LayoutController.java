package com.teralogic.useradminlayouts.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.teralogic.useradminlayouts.models.Layout;
import com.teralogic.useradminlayouts.payload.request.AssignRequest;
import com.teralogic.useradminlayouts.payload.response.LayoutResponse;
import com.teralogic.useradminlayouts.repository.LayoutRepository;
import com.teralogic.useradminlayouts.security.services.LayoutService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.teralogic.useradminlayouts.models.ERole;
import com.teralogic.useradminlayouts.models.Role;
import com.teralogic.useradminlayouts.models.User;
import com.teralogic.useradminlayouts.payload.request.LoginRequest;
import com.teralogic.useradminlayouts.payload.request.SignupRequest;
import com.teralogic.useradminlayouts.payload.response.UserInfoResponse;
import com.teralogic.useradminlayouts.payload.response.MessageResponse;
import com.teralogic.useradminlayouts.repository.RoleRepository;
import com.teralogic.useradminlayouts.repository.UserRepository;
import com.teralogic.useradminlayouts.security.jwt.JwtUtils;
import com.teralogic.useradminlayouts.security.services.UserDetailsImpl;

@RestController
@RequestMapping("/api/auth")
public class LayoutController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  LayoutRepository layoutRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  private LayoutService layoutService;


  //2.Create one GET api to display list of available layouts for the admin
  //5. Create GET api to fetch the layout details when user login.
  @GetMapping("/getlayouts")
  public ResponseEntity<?> getLayouts(@Valid @RequestParam String username) {

    Optional<User> userOptional = userRepository.findByUsername(username);

    List<String> layouts = null;

    if (userOptional.isPresent()) {
      User user = userOptional.get();
      Set<Role> roles = user.getRoles();
      List<ERole> roleNames = roles.stream()
              .map(Role::getName)
              .collect(Collectors.toList());
      if(roleNames.get(0).name().equalsIgnoreCase("ROLE_ADMIN")){
        //2.Create one GET api to display list of available layouts for the admin
        layouts = layoutService.getAllLayouts().stream()
                .map(Layout::getName)
                .collect(Collectors.toList());
      } else {
        //5. Create GET api to fetch the layout details when user login.
        layouts = userRepository.findByUsername(username)
                .map(user1 -> user1.getLayout().stream()
                        .map(Layout::getName)
                        .collect(Collectors.toList()))
                .orElse(null);
      }
    }


    return ResponseEntity.ok()
            .body(new LayoutResponse(username,layouts));
  }
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    List<String> layouts;
    if(roles.contains("ROLE_ADMIN")) {
      //2.Create one GET api to display list of available layouts for the admin
      layouts = layoutService.getAllLayouts().stream()
              .map(Layout::getName)
              .collect(Collectors.toList());
    } else {
      //5. Create GET api to fetch the layout details when user login.
      layouts = userRepository.findByUsername(loginRequest.getUsername())
              .map(user -> user.getLayout().stream()
                      .map(Layout::getName)
                      .collect(Collectors.toList()))
              .orElse(null);
    }

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
        .body(new UserInfoResponse(userDetails.getId(),
                                   userDetails.getUsername(),
                                   userDetails.getEmail(),
                                   roles,layouts));
  }


  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
                         signUpRequest.getEmail(),
                         encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "admin":
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(adminRole);

          break;
        case "mod":
          Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(modRole);

          break;
        default:
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  //3. Create POST api to assign selected layout to the user/user groups
  //4. Create UPDATE api to change the layout
  @PostMapping("/assignlayout")
  public ResponseEntity<?> assignLayout(@Valid @RequestBody AssignRequest assignRequest) {
    System.out.println("okokokokkm huhnn o");
    if (!userRepository.existsByUsername(assignRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is not registered!"));
    }

    Optional<User> userOptional = userRepository.findByUsername(assignRequest.getUsername());
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      Set<Integer> strLayouts = assignRequest.getLayout();
      Set<Layout> layout = new HashSet<>();
      System.out.println("strLayouts : " + strLayouts);
      strLayouts.forEach(layoutId -> {
        Layout layoutIds = layoutRepository.findAllById(layoutId)
                .orElseThrow(() -> new RuntimeException("Error: Layout is not found."));
        System.out.println("layoutIds : " + layoutIds);
        layout.add(layoutIds);
      });

      System.out.println("layout : " + layout);
      user.setLayout(layout);
      userRepository.save(user);
      return ResponseEntity.ok(new MessageResponse("Layout assigned successfully to User : " + assignRequest.getUsername()));
    } else {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Layout is not assigned successfully to User : " + assignRequest.getUsername()));
    }
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {
    System.out.println("okokokokkmo hgbgbb");
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new MessageResponse("You've been signed out!"));
  }

//  @GetMapping("/user")
//  public Optional<Role> getAllLayouts() {
//    System.out.println("okokokokkmo hgbgbb admin");
//    return roleRepository.findByName(ERole.ROLE_MODERATOR);
//  }
}
