package com.example.labworknumber1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class LabWorkNumber1Application {

    public static void main(String[] args) {
        SpringApplication.run(LabWorkNumber1Application.class, args);
    }

}
