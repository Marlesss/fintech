package com.academy.fintech.agreement;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;

/**
 * Used to [de]serialize AgreementService.proto messages.
 */
public class Mapper {

    /**
     * deserialize {@link com.google.protobuf.Timestamp} field to {@link Date}
     *
     * @param timestamp field to deserialize
     * @return deserialized date
     */
    public static Date getDateFrom(Timestamp timestamp) {
        return Date.from(Instant.ofEpochSecond(timestamp.getSeconds()));
    }
}
