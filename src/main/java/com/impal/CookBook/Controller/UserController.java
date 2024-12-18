package com.impal.CookBook.Controller;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import com.impal.CookBook.Model.Recipe;
import com.impal.CookBook.Model.User;
import com.impal.CookBook.Payload.LoginRequest;
import com.impal.CookBook.Payload.RecipeCardResponse;
import com.impal.CookBook.Payload.SignupRequest;
import com.impal.CookBook.Payload.UserInfoResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@Controller
@RequestMapping("")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private RecipeService recipeService;


    @GetMapping("/login")
    public String Login() {
        return "login";
    }

    @GetMapping("/logout")
    public String Logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("userCookie", null);
        response.addCookie(cookie);
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String signupForm() {
        return "register";
    }
    
    @PostMapping("/login")
    public String login(LoginRequest request,  HttpServletResponse response) {
        try {
            User user = service.authenticateUser(request.getUsername(), request.getPassword());
            Cookie cookie = new Cookie("userCookie", user.getImdbId());
            response.addCookie(cookie);
            return "redirect:/homepage";
        }catch(Exception e) {
            return "/login?failed=true";
        }
    }

    @PostMapping("/register")
    public String register(SignupRequest request) {
        try {
            service.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
            return "login";
        }catch(Exception e) {
            return "homepage";
        }
    }
    

    @PostMapping("/api/account/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> payload) {
        try {
            User user = service.authenticateUser(payload.get("username"), payload.get("password"));
            UserInfoResponse userResponse = new UserInfoResponse(user.getImdbId(), user.getUsername(), user.getProfilePic());
            Cookie cookie = new Cookie("user_name", payload.get("username"));
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(userResponse);
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/api/account/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> payload) {
        try {
            service.registerUser(payload.get("username"), payload.get("email"), payload.get("password"));
            return new ResponseEntity<String>("register.User registered successfully!", HttpStatus.OK);
        }catch(Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/account/logout")
    public ResponseEntity<?> logoutUser() {
        try {
            return new ResponseEntity<String>("logout.User logged out successfully!", HttpStatus.OK);
        }catch(Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/account/{imdbId}")
    public String findUser(@PathVariable String imdbId, @CookieValue(value = "userCookie", defaultValue = "Guest") String cookie,
                            Model model) {
        try {
            UserInfoResponse userResponse = service.convertToResponse(service.findUserByImdbId(imdbId));
    
            User user = service.findUserByImdbId(imdbId);
            ArrayList<RecipeCardResponse> myRecipes = new ArrayList<>();
            ArrayList<RecipeCardResponse> bookmarks = new ArrayList<>();
            for (Recipe rp : user.getMyrecipes()) {
                myRecipes.add(recipeService.convertToResponseCard(rp));
            }
            for (Recipe rp : user.getBookmarks()) {
                bookmarks.add(recipeService.convertToResponseCard(rp));
            }

            model.addAttribute("myrecipes", myRecipes);
            model.addAttribute("bookmarks", bookmarks);
            model.addAttribute("cookie", cookie);
            model.addAttribute("userResponse", userResponse);
            return "profile";
        }catch (Exception e) {
            return "redirect:/homepage";
        }
    }
    
    
}