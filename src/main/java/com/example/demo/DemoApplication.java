package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("profile.dat")) {

            int all = fis.available();
            byte[] data = fis.readAllBytes();
            byte[] result = new byte[all];
            for (int i = 0; i < data.length; i++) {
                byte n = (byte) (data[i] + 1);
                result[i] = n;
            }
            FileOutputStream fos = new FileOutputStream("个人.pdf");
            fos.write(result);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SpringApplication.run(DemoApplication.class, args);
    }

}
