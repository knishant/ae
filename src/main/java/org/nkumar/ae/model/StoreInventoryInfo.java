package org.nkumar.ae.model;

import com.opencsv.bean.CsvBindByName;

import static org.nkumar.ae.util.StringUtil.codify;

public class StoreInventoryInfo implements Keyed
{
    @CsvBindByName
    private String storeId;
    @CsvBindByName
    private String SKU;
    @CsvBindByName
    private int sold;
    @CsvBindByName
    private int available;
    @CsvBindByName
    private int age;

    public String getStoreId()
    {
        return storeId;
    }

    public void setStoreId(String storeId)
    {
        this.storeId = storeId;
    }

    public String getSKU()
    {
        return SKU;
    }

    public void setSKU(String SKU)
    {
        this.SKU = codify(SKU);
    }

    public int getSold()
    {
        return sold;
    }

    public void setSold(int sold)
    {
        this.sold = sold;
    }

    public int getAvailable()
    {
        return available;
    }

    public void setAvailable(int available)
    {
        this.available = available;
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    @Override
    public String getKey()
    {
        return String.join(",", getStoreId(), getSKU());
    }
}
