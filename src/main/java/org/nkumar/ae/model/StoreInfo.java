package org.nkumar.ae.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import org.nkumar.ae.converter.GradeBeanField;

public final class StoreInfo implements Keyed
{
    @CsvBindByName
    private String storeId;
    @CsvBindByName
    private String name;
    //higher value is higher grade
    @CsvCustomBindByName(converter= GradeBeanField.class)
    private int grade;

    public String getStoreId()
    {
        return storeId;
    }

    public void setStoreId(String storeId)
    {
        this.storeId = storeId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getGrade()
    {
        return grade;
    }

    public void setGrade(int grade)
    {
        this.grade = grade;
    }

    @Override
    public String getKey()
    {
        return getStoreId();
    }
}
