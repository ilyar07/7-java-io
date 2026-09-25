package com.example.task02;

import java.io.IOException;

public class Task02Main {
    public static void main(String[] args) throws IOException {
        int prev = -1;
        int current;
        while ((current = System.in.read()) != -1) {
            if (prev == 13 && current == 10) {
                System.out.write(10);
                prev = -1;
                continue;
            }
            if (prev != -1) {
                System.out.write(prev);
            }
            prev = current;
        }
        if (prev != -1) {
            System.out.write(prev);
        }
        System.out.flush();
    }
}
