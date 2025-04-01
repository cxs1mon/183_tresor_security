package ch.bbw.pr.tresorbackend.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import static org.springframework.security.crypto.bcrypt.BCrypt.hashpw;

/**
 * PasswordEncryptionService
 * @author Peter Rutschmann
 */
@Service
public class PasswordEncryptionService {
   //todo ergänzen!

   public PasswordEncryptionService() {
      //todo anpassen!
   }

   public String hashPassword(String password) {
      return BCrypt.hashpw(password, BCrypt.gensalt());
   }
}
