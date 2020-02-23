package org.nkumar.ae.output;

import org.nkumar.ae.model.StoreAllocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class StoreProcessor
{
    private StoreProcessor()
    {
    }

    public static void storeAllocations(File path, List<StoreAllocation> list)
    {
        try (Writer out = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))
        {
            out.write("storeId,SKU,reason\n");
            for (StoreAllocation sa : list)
            {
                Map<String, String> allocations = sa.getAllocations();
                for (String sku : allocations.keySet())
                {
                    out.append(sa.getStoreId()).append(',');
                    out.append(sku).append(',');
                    out.append(allocations.get(sku)).append('\n');
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to write allocation csv", e);
        }
    }
}
