package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.service.RecaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Service
public class RecaptchaServiceImpl implements RecaptchaService {

    @Value("${CAPTCHA_SECRET}")
    private String secret;

    @Override
    public boolean verifyCaptcha(String token) {
        try {
            String url    = "https://www.google.com/recaptcha/api/siteverify";
            String params = "secret=" + secret + "&response=" + token;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(params.getBytes(StandardCharsets.UTF_8));

            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                return response.contains("\"success\": true");
            }
        } catch (Exception e) {
            return false;
        }
    }
}
