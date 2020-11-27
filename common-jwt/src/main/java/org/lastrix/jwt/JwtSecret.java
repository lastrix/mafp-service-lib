package org.lastrix.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Getter
public class JwtSecret {
    private static final String JKS_PATH = "/jwt.jks";
    private final Algorithm algorithm = createAlgorithm();

    private Algorithm createAlgorithm() {
        try (InputStream is = JwtSecret.class.getResourceAsStream(JKS_PATH)) {
            if (is == null) throw new FileNotFoundException(JKS_PATH);
            var ks = KeyStore.getInstance("JKS");
            ks.load(new BufferedInputStream(is), "qwerty".toCharArray());
            var key = (KeyStore.PrivateKeyEntry) ks.getEntry("1", new KeyStore.PasswordProtection("qwerty".toCharArray()));
            return Algorithm.RSA256((RSAPublicKey) key.getCertificate().getPublicKey(), (RSAPrivateKey) key.getPrivateKey());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize jwt secret", e);
        }
    }
}
