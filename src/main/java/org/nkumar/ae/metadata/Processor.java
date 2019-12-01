package org.nkumar.ae.metadata;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.SKUSimilarity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class Processor
{
    private Processor()
    {
    }

    public static List<SKUInfo> loadSKU(File path)
    {
        try (CSVReader r = new CSVReader(new FileReader(path)))
        {
            CsvToBean<SKUInfo> csvToBean = new CsvToBeanBuilder<SKUInfo>(r)
                    .withType(SKUInfo.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return csvToBean.parse();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static SKUSimilarity buildSKUSimilarity(List<SKUInfo> skuInfos)
    {
        Map<String, SKUInfo> skuInfoMap = skuInfos.stream().collect(Collectors.toMap(SKUInfo::getSKU, info -> info));
        //treeset is important as sorted semantics is used later
        Set<String> skus = new TreeSet<>(skuInfoMap.keySet());
        if (skus.size() != skuInfos.size())
        {
            throw new IllegalArgumentException("Duplicate skus passed in the list");
        }
        SKUSimilarity.Builder builder = SKUSimilarity.Builder.create(skus);
        for (String sku1 : skus)
        {
            for (String sku2 : skus)
            {
                //this is just to iterate over lower half of the matrix
                if (sku1.compareTo(sku2) >= 0)
                {
                    break;
                }
                SKUInfo sku1Info = skuInfoMap.get(sku1);
                SKUInfo sku2Info = skuInfoMap.get(sku2);
                int match = match(sku1Info, sku2Info);
                if (match == 0)
                {
                    builder.addExactMatch(sku1, sku2);
                }
                else if (match > 0)
                {
                    builder.addPartialMatch(sku1, sku2);
                }
            }
        }
        return builder.build();
    }

    //all these attributes must match
    static boolean matchFixedAttributes(SKUInfo info1, SKUInfo info2)
    {
        return info1.getMaterial() == info2.getMaterial() &&
                info1.getShape().equals(info2.getShape()) &&
                info1.getFrameColor().equals(info2.getFrameColor()) &&
                info1.getLensCoating().equals(info2.getLensCoating()) &&
                info1.getLensFill().equals(info2.getLensFill()) &&
                info1.getLensColor().equals(info2.getLensColor());
    }

    //return 0 for exact match, positive number for partial match and negative number for no match.
    static int match(SKUInfo info1, SKUInfo info2)
    {
        int dissimilarity = 0;
        if (!matchFixedAttributes(info1, info2))
        {
            return -1;
        }
        {
            //one level of price diff allowed
            int priceRangeDiff = Math.abs(info1.getPriceRange() - info2.getPriceRange());
            if (priceRangeDiff == 1)
            {
                dissimilarity++;
            }
            else if (priceRangeDiff > 1)
            {
                return -1;
            }
        }
        {
            //one level of gender difference is allowed
            int genderDiff = Math.abs(info1.getGender().getValue() - info2.getGender().getValue());
            if (genderDiff == 1)
            {
                dissimilarity++;
            }
            else if (genderDiff > 1)
            {
                return -1;
            }
        }
        {
            //one level of size difference is allowed
            int sizeDiff = Math.abs(info1.getSize().getValue() - info2.getSize().getValue());
            if (sizeDiff == 1)
            {
                dissimilarity++;
            }
            else if (sizeDiff > 1)
            {
                return -1;
            }
        }
        {
            if (!info1.getFrameFinish().equals(info2.getFrameFinish()))
            {
                dissimilarity++;
            }
        }
        {
            if (!info1.getLensFeature().equals(info2.getLensFeature()))
            {
                dissimilarity++;
            }
        }
        return dissimilarity;
    }
}
