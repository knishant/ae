package org.nkumar.ae.converter;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public final class GradeBeanField<I> extends AbstractBeanField<Integer, I>
{
    @Override
    protected Integer convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException
    {
        switch (value)
        {
            case "A+":
                return 1;
            case "A":
                return 2;
            case "B":
                return 5;
            case "C":
                return 8;
            case "D":
                return 11;
            default:
                throw new IllegalArgumentException("Grade " + value + " is not valid");
        }
    }
}
