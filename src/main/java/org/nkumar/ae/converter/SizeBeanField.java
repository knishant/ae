package org.nkumar.ae.converter;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import org.nkumar.ae.model.Size;

public final class SizeBeanField<I> extends AbstractBeanField<Size, I>
{
    @Override
    protected Size convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException
    {
        return Size.valueOf(value.toUpperCase());
    }
}
