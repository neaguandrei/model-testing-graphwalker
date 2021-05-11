package com.fmi.user.util;

import com.fmi.user.dao.entity.NoteEntity;
import com.fmi.user.dao.entity.UserEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataUtil {

    public static UserEntity createUser() {
        return UserEntity.builder()
                .email("andreineagu.c@gmail.com")
                .firstName("Andrei-Cosmin")
                .lastName("Neagu")
                .password("password")
                .phone("0725111997")
                .enabled(false)
                .notes(new ArrayList<>())
                .build();
    }

    public static NoteEntity createNote() {
        return NoteEntity.builder()
                .text("Note-Text-Something")
                .build();
    }

    public static String generateRandomPassword() {
        String password = UUID.randomUUID().toString().replace("-", "");
        return password.substring(0, 12);
    }
}
