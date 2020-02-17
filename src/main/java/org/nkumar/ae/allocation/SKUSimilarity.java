package org.nkumar.ae.allocation;

import org.nkumar.ae.model.SKUInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class SKUSimilarity
{
    private final Map<String, List<String>> exactMatchMap;
    private final Map<String, List<String>> partialMatchMap;

    private SKUSimilarity(Map<String, List<String>> exactMatchMap,
            Map<String, List<String>> partialMatchMap)
    {
        this.exactMatchMap = exactMatchMap;
        this.partialMatchMap = partialMatchMap;
    }

    /**
     * Return the list of skus which are exact match of the passed sku.
     * Result can be modified without impacting the internal data structure.
     *
     * @param sku
     * @return exact match set
     */
    List<String> getExactMatches(String sku)
    {
        return new ArrayList<>(exactMatchMap.getOrDefault(sku, Collections.emptyList()));
    }

    /**
     * Return the list of skus which are partial match of the passed sku. At-max one difference.
     * skus with difference in lower priority attribute will be earlier in the list.
     * Result can be modified without impacting the internal data structure.
     *
     * @param sku
     * @return partial match set
     */
    List<String> getPartialMatches(String sku)
    {
        return new ArrayList<>(partialMatchMap.getOrDefault(sku, Collections.emptyList()));
    }

    private static class Builder
    {
        private final Map<String, List<String>> exactMatchMap;
        private final Map<String, Map<String, Integer>> partialMatchMap;

        private Builder(Set<String> skus)
        {
            this.exactMatchMap = new TreeMap<>();
            this.partialMatchMap = new TreeMap<>();
            skus.forEach(sku -> {
                exactMatchMap.put(sku, new ArrayList<>());
                partialMatchMap.put(sku, new HashMap<>());
            });
        }

        private static Builder create(Set<String> skus)
        {
            return new Builder(skus);
        }

        private void addExactMatch(String sku1, String sku2)
        {
            this.exactMatchMap.get(sku1).add(sku2);
            this.exactMatchMap.get(sku2).add(sku1);
        }

        private void addPartialMatch(String sku1, String sku2, int rank)
        {
            this.partialMatchMap.get(sku1).put(sku2, rank);
            this.partialMatchMap.get(sku2).put(sku1, rank);
        }

        private SKUSimilarity build()
        {
            Map<String, List<String>> sortedPartialMatchMap = new HashMap<>();
            partialMatchMap.forEach((sku, pMap) -> {
                List<String> partialMatchingList = pMap.entrySet().stream()
                        .sorted(Comparator.comparingInt(Map.Entry::getValue))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                sortedPartialMatchMap.put(sku, partialMatchingList);
            });
            return new SKUSimilarity(exactMatchMap, sortedPartialMatchMap);
        }
    }

    public static SKUSimilarity buildSKUSimilarity(Map<String, SKUInfo> skuInfoMap)
    {
        //treeset is important as sorted semantics is used later
        Set<String> skus = new TreeSet<>(skuInfoMap.keySet());
        SKUSimilarity.Builder builder = SKUSimilarity.Builder.create(skus);
        for (String sku1 : skus)
        {
            for (String sku2 : skus)
            {
                //this is just to iterate over lower half of the matrix
                if (sku1.compareTo(sku2) > 0)
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
                    builder.addPartialMatch(sku1, sku2, match);
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
        int rank = 0;
        int dissimilarity = 0;
        {
            //one level of price diff allowed
            int priceRangeDiff = Math.abs(info1.getPriceRange() - info2.getPriceRange());
            if (priceRangeDiff == 1)
            {
                dissimilarity++;
                rank = 1;
            }
            else if (priceRangeDiff > 1)
            {
                return -1;
            }
        }
        {
            if (!info1.getLensFeature().equals(info2.getLensFeature()))
            {
                dissimilarity++;
                rank = 2;
            }
        }
        {
            if (!info1.getFrameFinish().equals(info2.getFrameFinish()))
            {
                dissimilarity++;
                rank = 3;
            }
        }
        {
            //one level of size difference is allowed
            int sizeDiff = Math.abs(info1.getSize().getValue() - info2.getSize().getValue());
            if (sizeDiff == 1)
            {
                dissimilarity++;
                rank = 4;
            }
            else if (sizeDiff > 1)
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
                rank = 100;
            }
            else if (genderDiff > 1)
            {
                return -1;
            }
        }
        if (dissimilarity == 0)
        {
            return 0;
        }
        else if (dissimilarity > 1)
        {
            //only one difference allowed
            return -1;
        }
        return rank;
    }
}
