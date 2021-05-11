package com.fmi.user.model;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;

    private String email;

    private String password;

    private String newPassword;

    private String firstName;

    private String lastName;

    private String phone;
}
