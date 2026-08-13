package com.amadeus.aud.libraries.engine.data.repositories.base;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseRepository<T> {

  private volatile T data;

  private String dataMD5Hash;

  private String dataSHA256Hash;

  private String dataVersion;

  public BaseRepository() {
    this(null);
  }

  public BaseRepository(T map) {
    this.data = map;
  }

  @SuppressWarnings("unchecked")
  public void addData(T data) {
    if (this.data == null) {
      setData(data);
      return;
    }

    if (data instanceof Map<?, ?> map && this.data instanceof Map<?, ?>) {
      ((Map<Object, Object>) this.data).putAll(map);
    } else if (data instanceof Set<?> set && this.data instanceof Set<?>) {
      ((Set<Object>) this.data).addAll(set);
    } else if (data instanceof List<?> list && this.data instanceof List<?>) {
      ((List<Object>) this.data).addAll(list);
    }
  }

  public long getDataSize() {
    if (data instanceof Iterable<?> iterable) {
      return iterable.spliterator().getExactSizeIfKnown();
    }
    if (data instanceof Object[] objects) {
      return objects.length;
    }
    if (data instanceof Map<?, ?> map) {
      return map.size();
    }
    if (data != null) {
      return 1;
    } else {
      return 0;
    }
  }

  public boolean isReady() {
    return data != null && getDataSize() > 0;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String getDataMD5Hash() {
    return dataMD5Hash;
  }

  public void setDataMD5Hash(String dataMD5Hash) {
    this.dataMD5Hash = dataMD5Hash;
  }

  public String getDataSHA256Hash() {
    return dataSHA256Hash;
  }

  public void setDataSHA256Hash(String dataSHA256Hash) {
    this.dataSHA256Hash = dataSHA256Hash;
  }

  public String getDataVersion() {
    return dataVersion;
  }

  public void setDataVersion(String dataVersion) {
    this.dataVersion = dataVersion;
  }
}
