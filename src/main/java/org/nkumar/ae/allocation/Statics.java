package org.nkumar.ae.allocation;

import org.nkumar.ae.model.GenderShape;
import org.nkumar.ae.model.SKUInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Statics
{
    private final Map<String, SKUInfo> skuInfoMap;

    private final SKUSimilarity similarity;

    public Statics(List<SKUInfo> skuInfoList)
    {
        this.skuInfoMap = skuInfoList.stream()
                .collect(Collectors.toMap(SKUInfo::getSKU, Function.identity()));
        this.similarity = SKUSimilarity.buildSKUSimilarity(skuInfoMap);
    }

    public SKUInfo getSkuInfo(String sku)
    {
        SKUInfo skuInfo = skuInfoMap.get(sku);
        Objects.requireNonNull(skuInfo, "sku is not valid : " + sku);
        return skuInfo;
    }

    public GenderShape getGenderShapeForSKU(String sku)
    {
        SKUInfo skuInfo = getSkuInfo(sku);
        return new GenderShape(skuInfo.getGender(), skuInfo.getShape());
    }

    public List<String> getExactMatchSkus(String sku)
    {
        return this.similarity.getExactMatches(sku);
    }

    public List<String> getPartialMatchSkus(String sku)
    {
        return this.similarity.getPartialMatches(sku);
    }
}
