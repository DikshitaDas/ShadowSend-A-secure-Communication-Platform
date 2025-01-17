package com.SuchoCryptoStego.User.service;


import com.SuchoCryptoStego.User.controller.DecryptionFeign;
import com.SuchoCryptoStego.User.controller.EncryptionFeign;
import com.SuchoCryptoStego.User.model.*;
import com.SuchoCryptoStego.User.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    DecryptionFeign decryptionFeign;
    @Autowired
    EncryptionFeign encryptionFeign;
    @Autowired
    JwtService jwtService;

    private BCryptPasswordEncoder encoder= new BCryptPasswordEncoder(12);
    public ResponseEntity<List<UserView>> findAll(String token) {
        String rph= jwtService.extractUserName(token);
       return new ResponseEntity<>(decryptionFeign.findAll(rph).getBody(),HttpStatus.OK);
    }

    public ResponseEntity<UserView> get(int id) {
        UserView userView=decryptionFeign.getMessage(id).getBody();
        userView.setSenderNumber(null);
        return  new ResponseEntity<>(userView,HttpStatus.OK);
    }

    public ResponseEntity<String> decryption(int id,String token) {
        String rph=jwtService.extractUserName(token);
        DMessageBody messageBody=new DMessageBody();
        UserView userView=decryptionFeign.getMessage(id).getBody();
        String sph=userView.getSenderNumber();
        String key=getKey(sph,rph);
        messageBody.setKey(key);
        messageBody.setRph(rph);


        return new ResponseEntity<>(decryptionFeign.decryption(id,messageBody).getBody(),HttpStatus.OK);
    }

    public ResponseEntity<String> encryption(String token, Message message,MultipartFile imgFile) {

        String sph= jwtService.extractUserName(token);
        Users user=userRepo.findByphNo(sph);
        MessageBody messageBody=new MessageBody();
        messageBody.setMessage(message.getMessage());
        messageBody.setSenderName(user.getName());
        String key=getKey(sph,message.getRph());
        messageBody.setKey(key);
        messageBody.setRph(message.getRph());
        messageBody.setImageName(imgFile.getOriginalFilename());
        messageBody.setImageType(imgFile.getContentType());
        messageBody.setTimeStamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try{
            messageBody.setImage(imgFile.getBytes());
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(encryptionFeign.encrypt(sph,messageBody).getBody(),HttpStatus.CREATED);
    }

    public ResponseEntity<String> addUser(Users user) {
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            if(!userRepo.existsById(user.getPhNo())){
                userRepo.save(user);
                return new ResponseEntity<>("Account Created",HttpStatus.CREATED);
            }
            else
                return new ResponseEntity<>("Phone number been already registered!!!",HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage().toString(),HttpStatus.OK);
        }
    }

    public String getKey(String sph,String rph) {
        Users sender=userRepo.findByphNo(sph);
        Users receiver=userRepo.findByphNo(rph);

        System.out.println("Attempting to retrieve sender with ID: " + sph);
        System.out.println("Sender found: " + (sender != null ? "Yes" : "No"));
        System.out.println("Attempting to retrieve receiver with ID: " + rph);
        System.out.println("Receiver found: " + (receiver != null ? "Yes" : "No"));

        if (sender!=null & receiver!=null){
            String s= sender.getName();
            String r= receiver.getName();
            StringBuffer str=new StringBuffer();
            str.append(s);
            str.append(r);
            s=str.reverse().toString();
            System.out.println(s);
            return s;
        }
        else {
            System.out.println("Sender Or Receiver Not Found");
            return "Sender Or Receiver Not Found";
        }

    }


    public ResponseEntity<List<UserView>> search(String keyword) {
        return new ResponseEntity<>(decryptionFeign.search(keyword).getBody(),HttpStatus.OK);
    }
}
