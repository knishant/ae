package org.nkumar.ae.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class SKUSimilarity
{
    private final Map<String, List<Set<String>>> map;

    private SKUSimilarity(Map<String, List<Set<String>>> map)
    {
        this.map = map;
    }

    /**
     * Return the set of skus which are exact match of the passed sku.
     * Result can be modified without impacting the internal data structure.
     * @param sku
     * @return exact match set
     */
    public Set<String> getExactMatches(String sku)
    {
        return new TreeSet<>(map.get(sku).get(0));
    }

    /**
     * Return the set of skus which are partial match of the passed sku.
     * Result can be modified without impacting the internal data structure.
     * @param sku
     * @return partial match set
     */
    public Set<String> getPartialMatches(String sku)
    {
        return new TreeSet<>(map.get(sku).get(1));
    }

    private static class Builder
    {
        private final Map<String, List<Set<String>>> map;

        private Builder(Set<String> skus)
        {
            this.map = new TreeMap<>();
            skus.forEach(sku -> {
                List<Set<String>> list = new ArrayList<>();
                list.add(new TreeSet<>());
                list.add(new TreeSet<>());
                map.put(sku, list);
            });
        }

        private static Builder create(Set<String> skus)
        {
            return new Builder(skus);
        }

        private void addExactMatch(String sku1, String sku2)
        {
            addMatch(0, sku1, sku2);
        }

        private void addPartialMatch(String sku1, String sku2)
        {
            addMatch(1, sku1, sku2);
        }

        private void addMatch(int index, String sku1, String sku2)
        {
            this.map.get(sku1).get(index).add(sku2);
            this.map.get(sku2).get(index).add(sku1);
        }

        private SKUSimilarity build()
        {
            return new SKUSimilarity(map);
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
        if (!matchFixedAttributes(info1, info2))
        {
            return -1;
        }
        int dissimilarity = 0;
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
