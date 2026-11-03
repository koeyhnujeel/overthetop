package com.zoonza.overthetop;

import org.springframework.boot.SpringApplication;

public class TestOverthetopApplication {

    public static void main(String[] args) {
        SpringApplication.from(OverthetopApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
