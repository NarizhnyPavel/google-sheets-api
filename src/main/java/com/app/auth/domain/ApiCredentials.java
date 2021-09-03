package com.app.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@Table(name = "credentials_tb")
public class ApiCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_credentials")
    @SequenceGenerator(name="seq_credentials", sequenceName="SEQ_CREDENTIALS", allocationSize = 1)
    private Long id;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "password")
    private String password;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "refresh_token")
    private String refreshToken;

}
