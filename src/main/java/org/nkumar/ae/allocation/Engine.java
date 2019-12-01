package org.nkumar.ae.allocation;

import org.nkumar.ae.model.StoreAllocation;
import org.nkumar.ae.model.WarehouseInventoryInfo;

import java.util.List;

public final class Engine
{
    private final WarehouseInventoryInfo whInfo;
    private final List<StoreModel> storeModels;

    public Engine(WarehouseInventoryInfo whInfo, List<StoreModel> storeModels)
    {
        this.whInfo = whInfo;
        this.storeModels = storeModels;
    }

    public List<StoreAllocation> allocate()
    {
        //TODO Impl
        throw new UnsupportedOperationException();
    }
}
