package com.example.research2.SpringBoot.util;

import java.util.*;
import java.util.stream.Collectors;

public class LanguageUtils {
    public static List<String> getAllLanguages() {
        Locale[] locales = Locale.getAvailableLocales();
        Set<String> languageSet = Arrays.stream(locales)
                .map(locale -> locale.getDisplayLanguage(Locale.forLanguageTag("ru")))
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toSet());

        List<String> languages = new ArrayList<>(languageSet);
        languages.sort(String::compareToIgnoreCase);
        return languages;
    }
}
