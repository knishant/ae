package org.nkumar.ae.model;

public enum Size
{
    SMALL(1), MEDIUM(2), LARGE(3), OVERSIZED(4);

    private final int value;

    Size(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
