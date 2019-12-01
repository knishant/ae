package org.nkumar.ae.util;

import java.util.Objects;

public class StringUtil
{
    private StringUtil()
    {
    }

    public static String codify(String str)
    {
        Objects.requireNonNull(str, "code string must be non null");
        return str.trim().toUpperCase().intern();
    }
}
