package org.nkumar.ae.model;

import com.opencsv.bean.CsvBindByName;

public final class StoreInfo
{
    @CsvBindByName
    private String storeId;
    @CsvBindByName
    private String name;
    //higher value is higher grade
    @CsvBindByName
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
}
