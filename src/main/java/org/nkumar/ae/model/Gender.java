package org.nkumar.ae.model;

public enum Gender
{
    MAN(1), UNISEX(2), WOMAN(3);
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
