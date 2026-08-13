package com.amadeus.aud.libraries.engine.data.repositories.core.db;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import jakarta.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BlockedPortsRepository extends ConcurrentRepository<BlockedPort> {
  final ConcurrentHashMap<String, RangeSet<Integer>> protocolBlockedPortRanges;

  public BlockedPortsRepository() {
    super();
    protocolBlockedPortRanges = new ConcurrentHashMap<>();
  }

  public @Nullable RangeSet<Integer> getBlockedPortRanges(@Nullable String protocol) {
    if (protocol == null) {
      return null;
    }

    return protocolBlockedPortRanges.computeIfAbsent(
        protocol,
        (String k) -> {
          ConcurrentMap<String, BlockedPort> data = getData();
          if (data == null) {
            return null;
          }
          RangeSet<Integer> rangeSet = TreeRangeSet.create();
          for (BlockedPort blockedPort : data.values()) {
            if (Objects.equals(
                protocol.toLowerCase(Locale.ROOT),
                blockedPort
                    .getData()
                    .getSpec()
                    .getProtocol()
                    .getValue()
                    .toLowerCase(Locale.ROOT))) {
              rangeSet.add(PortUtils.toRange(blockedPort.getData().getSpec().getPort()));
            }
          }
          return rangeSet;
        });
  }
}
