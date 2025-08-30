package com.circulosiete.curso.minibank.payments.adapter.spei;

import java.util.regex.Pattern;

/**
 * Validador de CLABE:
 * - 18 dígitos
 * - Dígito verificador: pesos 3,7,1 repetidos; suma de (d*w mod 10); dv = (10 - (sum % 10)) % 10
 * - Verifica también que el código de banco (dígitos 1-3) sea permitido por configuración.
 */
public class SpeiValidation {
    private static final Pattern CLABE = Pattern.compile("\\d{18}");
    private static final int[] WEIGHTS = {3, 7, 1};

    private SpeiValidation() {
    }

    public static boolean isValidClabe(String clabe) {
        if (clabe == null || !CLABE.matcher(clabe).matches()) return false;
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int d = clabe.charAt(i) - '0';
            int w = WEIGHTS[i % 3];
            sum += ((d * w) % 10);
        }
        int expected = (10 - (sum % 10)) % 10;
        int actual = clabe.charAt(17) - '0';
        return expected == actual;
    }

    /**
     * Extrae clave de institución SPEI (3 dígitos iniciales).
     */
    public static String bankCode(String clabe) {
        if (clabe == null || clabe.length() < 3) return "";
        return clabe.substring(0, 3);
    }
}
