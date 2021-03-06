package org.skywalking.apm.collector.stream.worker.impl.data;

/**
 * @author pengys5
 */
public interface Operation {
    String operate(String newValue, String oldValue);

    Long operate(Long newValue, Long oldValue);

    Float operate(Float newValue, Float oldValue);

    Integer operate(Integer newValue, Integer oldValue);

    Boolean operate(Boolean newValue, Boolean oldValue);

    byte[] operate(byte[] newValue, byte[] oldValue);
}
