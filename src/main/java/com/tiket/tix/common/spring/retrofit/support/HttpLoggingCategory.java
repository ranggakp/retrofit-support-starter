package com.tiket.tix.common.spring.retrofit.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just for marking category of http request and response logs.
 *
 * @author zakyalvan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpLoggingCategory {
    public static final Logger LOGGER = LoggerFactory.getLogger(HttpLoggingCategory.class);
}
