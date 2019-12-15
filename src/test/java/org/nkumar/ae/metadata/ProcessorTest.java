package org.nkumar.ae.metadata;

import org.junit.Test;
import org.nkumar.ae.input.LoadProcessor;
import org.nkumar.ae.model.Gender;
import org.nkumar.ae.model.Material;
import org.nkumar.ae.model.SKUInfo;
import org.nkumar.ae.model.Size;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public final class ProcessorTest
{
    @Test
    public void loadTest()
    {
        List<SKUInfo> skuInfos = LoadProcessor.loadSKU(new File("src/test/resources/skuinfo/skuinfo1.csv"));
        assertEquals(1, skuInfos.size());
        SKUInfo info = skuInfos.get(0);
        assertEquals("SKU-1", info.getSKU());
        assertEquals(Gender.MAN, info.getGender());
        assertEquals(Material.METAL, info.getMaterial());
        assertEquals("Aviator", info.getShape());
        assertEquals(Size.SMALL, info.getSize());
        assertEquals("Silver", info.getFrameColor());
        assertEquals("Matt", info.getFrameFinish());
        assertEquals("Mirror", info.getLensCoating());
        assertEquals("Solid", info.getLensFill());
        assertEquals("color", info.getLensColor());
        assertEquals("Polarized", info.getLensFeature());
        assertEquals(2, info.getPriceRange());
    }
}
