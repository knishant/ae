package org.nkumar.ae.converter;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import org.nkumar.ae.model.Material;

public final class MaterialBeanField<I> extends AbstractBeanField<Material, I>
{
    @Override
    protected Material convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException
    {
        return Material.valueOf(value.toUpperCase());
    }
}
