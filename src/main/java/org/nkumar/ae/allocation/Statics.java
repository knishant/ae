package org.nkumar.ae.allocation;

import org.nkumar.ae.model.GenderShape;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.StoreInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

//stores static information about skus and stores
//also stores similarity of skus
public final class Statics
{
    private final Map<String, SKUInfo> skuInfoMap;

    private final Set<String> validSKUs;

    private final SKUSimilarity similarity;

    private final Set<String> validStoreIds;

    public Statics(List<SKUInfo> skuInfoList, List<StoreInfo> storeInfoList)
    {
        this.skuInfoMap = skuInfoList.stream()
                .collect(Collectors.toMap(SKUInfo::getKey, Function.identity()));
        this.validSKUs = Collections.unmodifiableSet(skuInfoMap.keySet());
        this.similarity = SKUSimilarity.buildSKUSimilarity(skuInfoMap);
        Map<String, StoreInfo> storeInfoMap = storeInfoList.stream()
                .collect(Collectors.toMap(StoreInfo::getKey, Function.identity()));
        this.validStoreIds = Collections.unmodifiableSet(storeInfoMap.keySet());
    }

    public Set<String> getValidSKUs()
    {
        return validSKUs;
    }

    public Set<String> getValidStoreIds()
    {
        return validStoreIds;
    }

    SKUInfo getSkuInfo(String sku)
    {
        SKUInfo skuInfo = skuInfoMap.get(sku);
        Objects.requireNonNull(skuInfo, "sku is not valid : " + sku);
        return skuInfo;
    }

    GenderShape getGenderShapeForSKU(String sku)
    {
        SKUInfo skuInfo = getSkuInfo(sku);
        return new GenderShape(skuInfo.getGender(), skuInfo.getShape());
    }

    List<String> getExactMatchSkus(String sku)
    {
        return this.similarity.getExactMatches(sku);
    }

    List<String> getPartialMatchSkus(String sku)
    {
        return this.similarity.getPartialMatches(sku);
    }
}
