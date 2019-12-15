package org.nkumar.ae.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class GenderShapeSKUsMap
{
    private final Map<Gender, Map<String/*shape*/, Set<String>/*SKU*/>> exactMap;
    private final Map<Gender, Map<String/*shape*/, Set<String>/*SKU*/>> similarMap;

    private GenderShapeSKUsMap(Map<Gender, Map<String, Set<String>>> exactMap,
            Map<Gender, Map<String/*shape*/, Set<String>/*SKU*/>> similarMap)
    {
        this.exactMap = exactMap;
        this.similarMap = similarMap;
    }

    public static GenderShapeSKUsMap buildGenderShapeSKUsMap(
            List<SKUInfo> skuInfos, SKUSimilarity skuSimilarity)
    {
        //compute exact sku mapping
        Map<Gender, Map<String, Set<String>>> exactMap = new EnumMap<>(Gender.class);
        skuInfos.forEach(info -> {
            exactMap.computeIfAbsent(info.getGender(), gender -> new HashMap<>())
                    .computeIfAbsent(info.getShape(), shape -> new HashSet<>()).add(info.getSKU());
        });

        Map<Gender, Map<String, Set<String>>> similarMap = new EnumMap<>(Gender.class);
        exactMap.forEach((gender, exactShapeSKUsMap) -> {
            Map<String, Set<String>> similarShapeSKUsMap
                    = similarMap.computeIfAbsent(gender, gen -> new HashMap<>());
            exactShapeSKUsMap.keySet().forEach(shape -> {
                Set<String> exactSKUsSet = exactShapeSKUsMap.get(shape);
                Set<String> similarSKUsSet = exactSKUsSet.stream()
                        .flatMap(sku -> skuSimilarity.getPartialMatches(sku).stream())
                        .collect(Collectors.toSet());
                similarSKUsSet.removeAll(exactSKUsSet);
                similarShapeSKUsMap.put(shape, similarSKUsSet);
            });
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
        Map<Gender, Map<String, Set<String>>> map = exact ? exactMap : similarMap;
        return Collections.unmodifiableSet(map.getOrDefault(gender, Collections.emptyMap())
                .getOrDefault(shape, Collections.emptySet()));
    }

}
