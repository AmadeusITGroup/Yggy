package com.amadeus.aud.libraries.engine.data.producers.blobdata;

import com.amadeus.aud.libraries.engine.data.factories.BlobStrategyFactory;
import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.base.BaseProducer;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.FileSystem;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class BlobResponseProducer<U extends ModelElementsProducerContext<String>>
    extends BaseProducer<String, U> implements AutoCloseable {

  private final Vertx vertexHandle;

  private final FileSystem fileSystem;

  public <V extends BlobStrategyFactory<String, U>> BlobResponseProducer(V strategyFactory) {
    super(strategyFactory);
    this.vertexHandle = Vertx.vertx();
    this.fileSystem = FileSystem.newInstance(vertexHandle.fileSystem());
  }

  @Override
  public Uni<Collection<String>> prepareProduceExecutable(U context) {

    Pattern filter =
        Pattern.compile(Objects.requireNonNullElse(context.getBlobNameExpression(), ".*"));

    List<Uni<String>> fileUnis = new ArrayList<>();

    try (DirectoryStream<Path> directory =
        Files.newDirectoryStream(Path.of(context.getContainerName()))) {

      for (Path path : directory) {
        if (Files.isRegularFile(path) && filter.matcher(path.getFileName().toString()).matches()) {
          fileUnis.add(fileSystem.readFile(path.toString()).onItem().transform(Buffer::toString));
        }
      }
      return Uni.combine().all().unis(fileUnis).with(String.class, Set::copyOf);

    } catch (IOException e) {
      return Uni.createFrom().failure(e);
    }
  }

  @Override
  public void close() {
    vertexHandle.close();
  }
}
