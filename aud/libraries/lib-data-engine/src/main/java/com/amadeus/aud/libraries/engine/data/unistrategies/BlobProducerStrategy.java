package com.amadeus.aud.libraries.engine.data.unistrategies;

import com.amadeus.aud.libraries.engine.data.producers.blobdata.contexts.ModelElementsBlobProducerContext;

public class BlobProducerStrategy<T, U extends ModelElementsBlobProducerContext<? extends T>>
    extends BaseProducerStrategy<T, U> {}
