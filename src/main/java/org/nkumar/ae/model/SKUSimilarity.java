package org.nkumar.ae.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class SKUSimilarity
{
    private final Map<String, List<Set<String>>> map;

    private SKUSimilarity(Map<String, List<Set<String>>> map)
    {
        this.map = map;
    }

    /**
     * Return the set of skus which are exact match of the passed sku.
     * Result can be modified without impacting the internal data structure.
     * @param sku
     * @return exact match set
     */
    public Set<String> getExactMatches(String sku)
    {
        return new TreeSet<>(map.get(sku).get(0));
    }

    /**
     * Return the set of skus which are partial match of the passed sku.
     * Result can be modified without impacting the internal data structure.
     * @param sku
     * @return partial match set
     */
    public Set<String> getPartialMatches(String sku)
    {
        return new TreeSet<>(map.get(sku).get(1));
    }

    public static class Builder
    {
        private final Map<String, List<Set<String>>> map;

        private Builder(Set<String> skus)
        {
            this.map = new TreeMap<>();
            skus.forEach(sku -> {
                List<Set<String>> list = new ArrayList<>();
                list.add(new TreeSet<>());
                list.add(new TreeSet<>());
                map.put(sku, list);
            });
        }

        public static Builder create(Set<String> skus)
        {
            return new Builder(skus);
        }

        public void addExactMatch(String sku1, String sku2)
        {
            addMatch(0, sku1, sku2);
        }

        public void addPartialMatch(String sku1, String sku2)
        {
            addMatch(1, sku1, sku2);
        }

        private void addMatch(int index, String sku1, String sku2)
        {
            this.map.get(sku1).get(index).add(sku2);
            this.map.get(sku2).get(index).add(sku1);
        }

        public SKUSimilarity build()
        {
            return new SKUSimilarity(map);
        }
    }
}
