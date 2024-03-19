package com.academy.fintech.origination.core.db.client;


import com.academy.fintech.application.ClientData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.checkerframework.common.aliasing.qual.Unique;

import java.math.BigInteger;

/**
 * entity для таблица "client"
 */
@Data
@ToString
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Entity
@Table(name = "client")
public class ClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Unique
    @Column(name = "email")
    private String email;

    @Column(name = "salary")
    private int salary;

    public static ClientEntity from(ClientData clientData) {
        return ClientEntity.builder()
                .firstName(clientData.getFirstName())
                .lastName(clientData.getLastName())
                .email(clientData.getEmail())
                .salary(clientData.getSalary())
                .build();
    }

    public ClientData to() {
        return ClientData.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email)
                .setSalary(salary)
                .build();
    }
}
