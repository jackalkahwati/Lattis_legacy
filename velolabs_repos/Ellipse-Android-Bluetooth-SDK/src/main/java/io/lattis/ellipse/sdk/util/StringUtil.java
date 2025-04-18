package io.lattis.ellipse.sdk.util;

public class StringUtil {

    public static String extractSerialNumber(String number){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i< number.length();i++){
            if(Character.isLetter(number.charAt(i))||Character.isDigit(number.charAt(i))){
                stringBuilder.append(number.charAt(i));
            } else {
                break;
            }
        }
        return stringBuilder.toString();
    }
}
