package com.example.importsapi.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SECRET = "clave-secreta-para-tests-muy-larga-minimo-32-chars-ok";
    private static final String TAX_ID = "12-3456789-0";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationMs(3600000L);
        jwtService = new JwtService(props);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("genera un token no nulo")
        void generateToken_notNull() {
            assertThat(jwtService.generateToken(TAX_ID)).isNotBlank();
        }

        @Test
        @DisplayName("el token generado es válido")
        void generateToken_isValid() {
            String token = jwtService.generateToken(TAX_ID);
            assertThat(jwtService.isValid(token)).isTrue();
        }
    }

    @Nested
    @DisplayName("extractTaxId")
    class ExtractTaxId {

        @Test
        @DisplayName("extrae el taxId correcto del token")
        void extractTaxId_correct() {
            String token = jwtService.generateToken(TAX_ID);
            assertThat(jwtService.extractTaxId(token)).isEqualTo(TAX_ID);
        }
    }

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @Test
        @DisplayName("retorna true para un token válido")
        void isValid_validToken() {
            String token = jwtService.generateToken(TAX_ID);
            assertThat(jwtService.isValid(token)).isTrue();
        }

        @Test
        @DisplayName("retorna false para un token expirado")
        void isValid_expiredToken() {
            JwtProperties expiredProps = new JwtProperties();
            expiredProps.setSecret(SECRET);
            expiredProps.setExpirationMs(-1000L);
            JwtService expiredJwtService = new JwtService(expiredProps);

            String expiredToken = expiredJwtService.generateToken(TAX_ID);
            assertThat(jwtService.isValid(expiredToken)).isFalse();
        }

        @Test
        @DisplayName("retorna false para un string aleatorio")
        void isValid_randomString() {
            assertThat(jwtService.isValid("esto.no.es.un.token")).isFalse();
        }

        @Test
        @DisplayName("retorna false para un token manipulado")
        void isValid_tamperedToken() {
            String token = jwtService.generateToken(TAX_ID);
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";
            assertThat(jwtService.isValid(tampered)).isFalse();
        }

        @Test
        @DisplayName("retorna false para un token firmado con otra clave")
        void isValid_differentSecret() {
            JwtProperties otherProps = new JwtProperties();
            otherProps.setSecret("otra-clave-completamente-diferente-para-tests-ok");
            otherProps.setExpirationMs(3600000L);
            JwtService otherService = new JwtService(otherProps);

            String tokenFromOtherKey = otherService.generateToken(TAX_ID);
            assertThat(jwtService.isValid(tokenFromOtherKey)).isFalse();
        }
    }
}
