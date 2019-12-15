package org.nkumar.ae.model;

import java.util.Objects;

public final class GenderShape
{
    private final Gender gender;
    private final String shape;

    public GenderShape(Gender gender, String shape)
    {
        this.gender = gender;
        this.shape = shape;
    }

    public Gender getGender()
    {
        return gender;
    }

    public String getShape()
    {
        return shape;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        GenderShape that = (GenderShape) o;
        return gender == that.gender &&
                shape.equals(that.shape);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(gender, shape);
    }

    @Override
    public String toString()
    {
        return "GenderShape{" +
                "gender=" + gender +
                ", shape='" + shape + '\'' +
                '}';
    }
}
