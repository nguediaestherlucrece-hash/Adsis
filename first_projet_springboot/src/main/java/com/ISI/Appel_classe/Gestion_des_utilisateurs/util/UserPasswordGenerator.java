package com.ISI.Appel_classe.Gestion_des_utilisateurs.util;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

public final class UserPasswordGenerator {

    private static final int GENERATED_PASSWORD_LENGTH = 12;

    private UserPasswordGenerator() {
        // Utility class
    }

    public static String generateRandomPassword() {
        PasswordGenerator generator = new PasswordGenerator();
        CharacterRule lowerCaseRule = new CharacterRule(EnglishCharacterData.LowerCase, 2);
        CharacterRule upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digitRule = new CharacterRule(EnglishCharacterData.Digit, 2);
        CharacterRule specialRule = new CharacterRule(EnglishCharacterData.Special, 2);

        return generator.generatePassword(GENERATED_PASSWORD_LENGTH, lowerCaseRule, upperCaseRule, digitRule, specialRule);
    }
}
