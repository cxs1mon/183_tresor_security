package ch.bbw.pr.tresorbackend.service;

import lombok.Value;

public interface RecaptchaService {
    boolean verifyCaptcha(String token);
}
