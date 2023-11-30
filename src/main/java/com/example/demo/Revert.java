package com.example.demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Revert {
    //29132e88276afb5f17651764caa5055c
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("个人.pdf")) {

            int all = fis.available();
            byte[] data = fis.readAllBytes();
            byte[] result = new byte[all];
            for (int i = 0; i < data.length; i++) {
                byte n = (byte) (data[i] - 1);
                result[i] = n;
            }
            FileOutputStream fos = new FileOutputStream("profile.data2");
            fos.write(result);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
