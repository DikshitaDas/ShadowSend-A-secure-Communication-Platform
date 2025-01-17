package com.SuchoCryptoStego.User.controller;


import com.SuchoCryptoStego.User.model.*;
import com.SuchoCryptoStego.User.service.JwtService;
import com.SuchoCryptoStego.User.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;

    /*
    This Commented area is for anyone who
    are developing frontend based on this backend
    Welcome to my world here every message is secure and safe
    I'm no Zuckerberg, so there is no way
    to steal data
    */

    //Get the list of unread messages
    @GetMapping("/findAll")
    public ResponseEntity<List<UserView>> findAll
            (@RequestHeader("Authorization") String authHeader){
        String token=authHeader.substring(7);
        return userService.findAll(token);
    }

    //Call this api after user clicking on a particular message
    @GetMapping("/get/{id}")
    public ResponseEntity<UserView> getMessage(@PathVariable int id){
        return userService.get(id);
    }

    //search for messages using phone number or sender name
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<UserView>> search(@PathVariable String keyword){
        return userService.search(keyword);
    }

    //Call this api when the user click on decrypt
    //This will decrypt the message and delete the whole record from database
    //Deletion is for provide more data security
    @PostMapping("/decrypt")
    public ResponseEntity<String> decryption
            (@RequestParam("id") int id,@RequestHeader("Authorization") String authHeader){
        String token=authHeader.substring(7);
        return userService.decryption(id,token);
    }

    //Whenever the user wants to send message
    // (Create a form for user to provide senders number,message and picture)
    @PostMapping("/encrypt")
    public ResponseEntity<String> encrypt
            (@RequestHeader("Authorization") String authHeader, @RequestPart("message") Message message, @RequestPart("image") MultipartFile imgFile){
        String token=authHeader.substring(7);
        return userService.encryption(token,message,imgFile);
    }

    //For signup
    @PostMapping("/signup")
    public ResponseEntity<String> signIn(@RequestBody Users user){
        return userService.addUser(user);
    }

    //For Login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Users user){
        Authentication auth=authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getPhNo(),user.getPassword()));
        if (auth.isAuthenticated()){
            return new ResponseEntity<>(jwtService.generateToken(user.getPhNo()), HttpStatus.OK);
        }
        else
            return new ResponseEntity<>("Invalid credentials!!!", HttpStatus.UNAUTHORIZED);
    }

    /*
    This is for later update whenever
    it feels like switching from Http to Websocket
    until then just keep your user under a illusion
    */

////    @MessageMapping("/encrypt")
////    public void encryptMessage(@Payload EncryptRequest encryptRequest) {
////        String token = encryptRequest.getToken();
////        Message message = encryptRequest.getMessage();
////        MultipartFile imgFile = encryptRequest.getImageFile();
////
////        messagingTemplate.convertAndSendToUser(
////                message.getRph(),           // Destination user identified by their phone number
////                "/queue/encryptedMessage", // The destination within the user-specific queue
////                userService.encryption(token, message, imgFile).getBody() // The message payload to send
////        );
//    }

}
