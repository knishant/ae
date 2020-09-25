package org.nkumar.ae.converter;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public final class AgeIntField<I> extends AbstractBeanField<Integer, I>
{
    @Override
    protected Integer convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException
    {
        //this is to handle values like 180+
        if (value.endsWith("+"))
            value = value.substring(0, value.length()-1);
        return Integer.parseInt(value);
    }
}
