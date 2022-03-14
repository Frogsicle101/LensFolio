package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;

public class TimeService {

    /**
     * Gets the current time as a protobuf timestamp
     */
    public static Timestamp getTimeStamp() {
        long millis = System.currentTimeMillis();

        Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
                .setNanos((int) ((millis % 1000) * 1000000)).build();

        return timestamp;
    }



}
