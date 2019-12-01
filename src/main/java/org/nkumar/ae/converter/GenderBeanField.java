package org.nkumar.ae.converter;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import org.nkumar.ae.model.Gender;

public final class GenderBeanField<I> extends AbstractBeanField<Gender, I>
{
    @Override
    protected Gender convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException
    {
        return Gender.valueOf(value.toUpperCase());
    }
}
