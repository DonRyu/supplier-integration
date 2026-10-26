package com.donryu.supplier.feed;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FeedMapperRegistry {

    private final Map<FeedFormat, FeedMapper> mappers = new EnumMap<>(FeedFormat.class);

    public FeedMapperRegistry(List<FeedMapper> mappers) {
        mappers.forEach(mapper -> this.mappers.put(mapper.format(), mapper));
    }

    public FeedMapper get(FeedFormat format) {
        FeedMapper mapper = mappers.get(format);
        if (mapper == null) {
            throw new FeedParseException("Unsupported feed format: " + format);
        }
        return mapper;
    }
}
