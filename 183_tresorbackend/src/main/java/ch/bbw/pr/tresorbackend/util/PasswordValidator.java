package ch.bbw.pr.tresorbackend.util;

public class PasswordValidator {

    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[a-z].*")) return false;
        if (!password.matches(".*\\d.*")) return false;
        if (!password.matches(".*[!@#$%^&*].*")) return false;
        return true;
    }
}
