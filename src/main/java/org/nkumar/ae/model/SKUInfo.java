package org.nkumar.ae.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import org.nkumar.ae.converter.GenderBeanField;
import org.nkumar.ae.converter.MaterialBeanField;
import org.nkumar.ae.converter.SizeBeanField;

public final class SKUInfo implements Keyed
{
    @CsvBindByName
    private String SKU;
    @CsvCustomBindByName(converter = GenderBeanField.class)
    private Gender gender;
    @CsvCustomBindByName(converter = MaterialBeanField.class)
    private Material material;
    @CsvBindByName
    private String shape;
    @CsvCustomBindByName(converter = SizeBeanField.class)
    private Size size;
    @CsvBindByName
    private String frameColor;
    @CsvBindByName
    private String frameFinish;
    @CsvBindByName
    private String lensCoating;
    @CsvBindByName
    private String lensFill;
    @CsvBindByName
    private String lensColor;
    @CsvBindByName
    private String lensFeature;
    @CsvBindByName
    private int priceRange;

    public String getSKU()
    {
        return SKU;
    }

    public void setSKU(String SKU)
    {
        this.SKU = SKU;
    }

    public Gender getGender()
    {
        return gender;
    }

    public void setGender(Gender gender)
    {
        this.gender = gender;
    }

    public Material getMaterial()
    {
        return material;
    }

    public void setMaterial(Material material)
    {
        this.material = material;
    }

    public String getShape()
    {
        return shape;
    }

    public void setShape(String shape)
    {
        this.shape = shape;
    }

    public Size getSize()
    {
        return size;
    }

    public void setSize(Size size)
    {
        this.size = size;
    }

    public String getFrameColor()
    {
        return frameColor;
    }

    public void setFrameColor(String frameColor)
    {
        this.frameColor = frameColor;
    }

    public String getFrameFinish()
    {
        return frameFinish;
    }

    public void setFrameFinish(String frameFinish)
    {
        this.frameFinish = frameFinish;
    }

    public String getLensCoating()
    {
        return lensCoating;
    }

    public void setLensCoating(String lensCoating)
    {
        this.lensCoating = lensCoating;
    }

    public String getLensFill()
    {
        return lensFill;
    }

    public void setLensFill(String lensFill)
    {
        this.lensFill = lensFill;
    }

    public String getLensColor()
    {
        return lensColor;
    }

    public void setLensColor(String lensColor)
    {
        this.lensColor = lensColor;
    }

    public String getLensFeature()
    {
        return lensFeature;
    }

    public void setLensFeature(String lensFeature)
    {
        this.lensFeature = lensFeature;
    }

    public int getPriceRange()
    {
        return priceRange;
    }

    public void setPriceRange(int priceRange)
    {
        this.priceRange = priceRange;
    }

    @Override
    public String getKey()
    {
        return getSKU();
    }

    @Override
    public String toString()
    {
        return "SKUInfo{" +
                "SKU='" + SKU + '\'' +
                ", gender=" + gender +
                ", material=" + material +
                ", shape='" + shape + '\'' +
                ", size=" + size +
                ", frameColor='" + frameColor + '\'' +
                ", frameFinish='" + frameFinish + '\'' +
                ", lensCoating='" + lensCoating + '\'' +
                ", lensFill='" + lensFill + '\'' +
                ", lensColor='" + lensColor + '\'' +
                ", lensFeature='" + lensFeature + '\'' +
                ", priceRange=" + priceRange +
                '}';
    }
}
