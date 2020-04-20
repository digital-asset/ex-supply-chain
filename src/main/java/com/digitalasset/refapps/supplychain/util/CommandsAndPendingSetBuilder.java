/**
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.supplychain.util;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.SubmitCommandsRequest;
import com.daml.ledger.rxjava.components.helpers.CommandsAndPendingSet;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

/**
 * Bots' "calculateCommands" need to emit an object containing commands to execute and a pending
 * contract set. This builder simplifies that task.
 */
public class CommandsAndPendingSetBuilder {
  public static Factory factory(
      String applicationId, Supplier<Clock> clockSupplier, Duration mrtDuration) {
    return new Factory(applicationId, clockSupplier, mrtDuration);
  }

  public static class Factory {
    private final String applicationId;
    private final Supplier<Clock> clockSupplier;
    private Duration mrtDuration;

    public Factory(String applicationId, Supplier<Clock> clockSupplier, Duration mrtDuration) {
      this.applicationId = applicationId;
      this.clockSupplier = clockSupplier;
      this.mrtDuration = mrtDuration;
    }

    public CommandsAndPendingSetBuilder create(String party, String workflowId) {
      return new CommandsAndPendingSetBuilder(
          applicationId, party, workflowId, clockSupplier, mrtDuration);
    }
  }

  private final String appId;
  private final String party;
  private final String workflowId;
  private final Supplier<Clock> clockSupplier;
  private final Duration mrtDuration;

  CommandsAndPendingSetBuilder(
      String appId,
      String party,
      String workflowId,
      Supplier<Clock> clockSupplier,
      Duration mrtDuration) {
    this.appId = appId;
    this.party = party;
    this.workflowId = workflowId;
    this.clockSupplier = clockSupplier;
    this.mrtDuration = mrtDuration;
  }

  public Builder newBuilder() {
    return new Builder();
  }

  public final class Builder {
    private List<Command> commands = new ArrayList<>();
    private Map<Identifier, PSet<String>> pendingContractIds = new HashMap<>();

    public void addCommand(Command cmd) {
      commands.add(cmd);
      if (cmd instanceof ExerciseCommand) {
        ExerciseCommand ecmd = (ExerciseCommand) cmd;
        pendingContractIds.compute(
            ecmd.getTemplateId(),
            (k, v) ->
                (v == null)
                    ? HashTreePSet.singleton(ecmd.getContractId())
                    : v.plus(ecmd.getContractId()));
      }
    }

    public Optional<CommandsAndPendingSet> build() {
      if (commands.isEmpty() && pendingContractIds.isEmpty()) {
        return Optional.empty();
      } else {
        Optional<Instant> now = Optional.of(clockSupplier.get().instant());
        Optional<Duration> mrt = Optional.of(mrtDuration);
        SubmitCommandsRequest commandsRequest =
            new SubmitCommandsRequest(
                workflowId, appId, UUID.randomUUID().toString(), party, now, mrt, Optional.empty(), commands);
        return Optional.of(
            new CommandsAndPendingSet(commandsRequest, HashTreePMap.from(pendingContractIds)));
      }
    }

    public Flowable<CommandsAndPendingSet> buildFlowable() {
      return build().map(Maybe::just).orElse(Maybe.empty()).toFlowable();
    }
  }
}
