package org.nkumar.ae.model;

public final class StoreAllocation
{
    private final String storeId;
    private final String sku;
    private final String mode;
    private final String replenishmentFor;

    public StoreAllocation(String storeId, String sku, String mode, String replenishmentFor)
    {
        this.storeId = storeId;
        this.sku = sku;
        this.mode = mode;
        this.replenishmentFor = replenishmentFor;
    }

    public String getStoreId()
    {
        return storeId;
    }

    public String getSku()
    {
        return sku;
    }

    public String getMode()
    {
        return mode;
    }

    public String getReplenishmentFor()
    {
        return replenishmentFor;
    }
}
