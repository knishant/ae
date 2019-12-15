package org.nkumar.ae.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class GenderShapeSKUsMap
{
    private final Map<GenderShape, Set<String>/*SKU*/> exactMap;
    private final Map<GenderShape, Set<String>/*SKU*/> similarMap;

    private GenderShapeSKUsMap(Map<GenderShape, Set<String>> exactMap,
            Map<GenderShape, Set<String>/*SKU*/> similarMap)
    {
        this.exactMap = exactMap;
        this.similarMap = similarMap;
    }

    public static GenderShapeSKUsMap buildGenderShapeSKUsMap(
            List<SKUInfo> skuInfos, SKUSimilarity skuSimilarity)
    {
        //compute exact sku mapping
        Map<GenderShape, Set<String>> exactMap = new HashMap<>();
        skuInfos.forEach(info -> {
            GenderShape genderShape = new GenderShape(info.getGender(), info.getShape());
            exactMap.computeIfAbsent(genderShape, key -> new HashSet<>()).add(info.getSKU());
        });

        Map<GenderShape, Set<String>> similarMap = new HashMap<>();
        exactMap.keySet().forEach(genderShape -> {
                Set<String> exactSKUsSet = exactMap.get(genderShape);
                Set<String> similarSKUsSet = exactSKUsSet.stream()
                        .flatMap(sku -> skuSimilarity.getPartialMatches(sku).stream())
                        .collect(Collectors.toSet());
                similarSKUsSet.removeAll(exactSKUsSet);
                similarMap.put(genderShape, similarSKUsSet);
        });
        return new GenderShapeSKUsMap(exactMap, similarMap);
    }

    /**
     * Return a set of skus that have the gender and shape that has been passed.
     * If exact is false then return any sku which is similar to sku which has this gender and shape.
     * May return empty set.
     */
    public Set<String> getSKUs(Gender gender, String shape, boolean exact)
    {
        Map<GenderShape,  Set<String>> map = exact ? exactMap : similarMap;
        return Collections.unmodifiableSet(map.getOrDefault(new GenderShape(gender, shape), Collections.emptySet()));
    }

}
