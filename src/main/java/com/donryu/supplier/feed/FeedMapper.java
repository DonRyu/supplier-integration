package com.donryu.supplier.feed;

import java.io.InputStream;

public interface FeedMapper {

    FeedFormat format();

    FeedMapResult map(InputStream input);
}
