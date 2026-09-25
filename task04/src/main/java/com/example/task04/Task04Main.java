package com.example.task04;

import java.io.IOException;
import java.util.Scanner;
import java.util.Locale;

public class Task04Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        double sum = 0;
        while (scanner.hasNext()) {
            String token = scanner.next();
            try {
                sum += Double.parseDouble(token);
            } catch (NumberFormatException ignored) {
            }
        }
        System.out.printf(Locale.US, "%.6f%n", sum);
    }
}
