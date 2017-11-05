package com.selesse.dates;

import com.google.common.collect.Lists;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FlexibleDateParser {
    private List<ThreadLocal<SimpleDateFormat>> threadLocals;
    private static final List<String> formats = Lists.newArrayList(
            "yyyy-MM-dd HH:mm:ss Z",
            "EEE MMM d HH:mm:ss yyyy Z",
            "EEE MMM d HH:mm:ss z yyyy"
    );

    public FlexibleDateParser() {
        threadLocals = Lists.newArrayList();

        for (final String format : formats) {
            ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                    dateFormat.setLenient(false);
                    return dateFormat;
                }
            };
            threadLocals.add(dateFormatThreadLocal);
        }
    }

    public Date parseDate(String dateStr) throws ParseException {
        for (ThreadLocal<SimpleDateFormat> dateFormatThreadLocal : threadLocals) {
            SimpleDateFormat dateFormat = dateFormatThreadLocal.get();
            try {
                return dateFormat.parse(dateStr);
            } catch (ParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Did not know how to parse date string: " + dateStr);
    }
}
