package com.example.research2.SpringBoot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CountryUtils {
    public static List<String> getAllCountries() {
        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry(Locale.forLanguageTag("ru"))); // или "en"
        }
        Collections.sort(countries); // сортировка по алфавиту
        return countries;
    }
}

