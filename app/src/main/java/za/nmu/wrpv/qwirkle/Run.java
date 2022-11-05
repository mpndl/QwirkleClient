package za.nmu.wrpv.qwirkle;

import android.content.Context;

import java.util.Map;

@FunctionalInterface
public interface Run {
    void run(Map<String, Object> data);
}
