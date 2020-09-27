package org.nkumar.ae.output;


import org.nkumar.ae.model.StoreAllocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class StoreProcessor
{
    private StoreProcessor()
    {
    }

    public static void storeAllocations(File path, List<StoreAllocation> list)
    {
        try (Writer out = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))
        {
            out.write("storeId,SKU,mode,replenishmentFor\n");
            for (StoreAllocation sa : list)
            {
                out.append(sa.getStoreId()).append(',');
                out.append(sa.getSku()).append(',');
                out.append(sa.getMode()).append(',');
                out.append(sa.getReplenishmentFor()).append('\n');
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to write allocation csv", e);
        }
    }
}
