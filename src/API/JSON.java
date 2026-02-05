package API;

import exception.InvalidInput;

public class JSON {
    // works for simple flat JSON like {"name":"Tea","price":500,"category":"Drink"}
    public static String simpleString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int k = json.indexOf(pattern);
        if (k < 0) throw new InvalidInput("Missing field: " + key);
        int colon = json.indexOf(":", k);
        int firstQuote = json.indexOf("\"", colon + 1);
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) throw new InvalidInput("Invalid field: " + key);
        return json.substring(firstQuote + 1, secondQuote).trim();
    }

    public static double simpleDouble(String json, String key) {
        String pattern = "\"" + key + "\"";
        int k = json.indexOf(pattern);
        if (k < 0) throw new InvalidInput("Missing field: " + key);
        int colon = json.indexOf(":", k);
        int end = findNumberEnd(json, colon + 1);
        String num = json.substring(colon + 1, end).trim();
        try { return Double.parseDouble(num); }
        catch (Exception e) { throw new InvalidInput("Invalid number: " + key); }
    }

    private static int findNumberEnd(String json, int start) {
        int i = start;
        while (i < json.length() && (json.charAt(i) == ' ')) i++;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (!(Character.isDigit(c) || c == '.' || c == '-')) break;
            i++;
        }
        return i;
    }
}
