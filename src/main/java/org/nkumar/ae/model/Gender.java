package org.nkumar.ae.model;

public enum Gender
{
    MEN(1), UNISEX(2), WOMEN(3);
    private final int value;

    Gender(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
