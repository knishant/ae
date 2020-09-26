package org.nkumar.ae.util;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import org.nkumar.ae.model.Keyed;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class CSVUtil
{
    private static final Logger LOG = Logger.getLogger(CSVUtil.class.getName());

    private CSVUtil()
    {
    }

    public static <T extends Keyed> List<T> loadCSV(File path, Class<? extends T> type)
    {
        try (CSVReader r = new CSVReader(new FileReader(path)))
        {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(r)
                    .withType(type)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            AtomicInteger ignoreCount = new AtomicInteger(0);
            List<T> list = csvToBean.parse().stream()
                    .filter(distinctByKey(path.getName(), ignoreCount))
                    .collect(Collectors.toList());
            if (ignoreCount.get() > 0)
            {
                LOG.log(Level.WARNING, "Ignored {1} duplicate keys while parsing {0}",
                        new Object[]{path, ignoreCount.get()});
            }
            return list;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Keyed> Predicate<T> distinctByKey(String path, AtomicInteger ignoreCount)
    {
        Map<Object, Boolean> seen = new HashMap<>();
        return t -> {
            String key = t.getKey();
            boolean nonExistent = seen.putIfAbsent(key, Boolean.TRUE) == null;
            if (!nonExistent)
            {
                ignoreCount.incrementAndGet();
                LOG.log(Level.FINE, "Ignoring duplicate key while parsing {0} : {1}", new Object[]{path, key});
            }
            return nonExistent;
        };
    }
}
