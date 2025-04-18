package com.lattis.ellipse.Utils;


import java.util.Currency;
import java.util.Locale;

public class CurrencyUtil {

    private static String defaulCurrencySymbol = "$";

    public static String getCurrencySymbolByCode(String code) {
        if(code!=null && !code.equals("")) {
            for (Currency currency : Currency.getAvailableCurrencies()) {
                if (currency.getCurrencyCode().equals(code)) {
                    return currency.getSymbol(Locale.US);
                }
            }
        }
        return defaulCurrencySymbol;
    }
}
