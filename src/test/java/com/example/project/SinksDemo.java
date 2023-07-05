package com.corporater.kestrel.participant.adapter.in.graphql;

import static reactor.util.concurrent.Queues.SMALL_BUFFER_SIZE;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

class SinksDemo {

    @Test
    void replayLatestReplays() {
        Sinks.Many<String> sink = Sinks.many().replay().latest();

        sink.tryEmitNext("Foo");

        StepVerifier.create(sink.asFlux()).expectNext("Foo").thenCancel().verify();
        StepVerifier.create(sink.asFlux()).expectNext("Foo").thenCancel().verify();
    }

    @Test
    void directAllOrNothingDoesNotReplay() {
        Sinks.Many<String> sink = Sinks.many().multicast().directAllOrNothing();

        sink.tryEmitNext("Foo");
        sink.tryEmitComplete();

        StepVerifier.create(sink.asFlux()).expectComplete().verify();
        StepVerifier.create(sink.asFlux()).expectComplete().verify();
    }

    @Test
    void onBackpressureBufferDoesNotShutDownWhenAutoCancelIsFalse() {
        boolean autoCancel = false;
        Sinks.Many<String> sink = Sinks
                .many()
                .multicast()
                .onBackpressureBuffer(SMALL_BUFFER_SIZE, autoCancel);

        StepVerifier.create(sink.asFlux()).thenCancel().verify();

        StepVerifier stepVerifier = StepVerifier
                .create(sink.asFlux())
                .expectNext("Bar")
                .thenCancel()
                .verifyLater();

        sink.tryEmitNext("Bar");

        // With autoCancel == false the sink does not shut down when the first subscriber cancels.
        stepVerifier.verify();
    }

    @Test
    void onBackpressureShutsDownWhenAutoCancelIsTrue() {
        // autoCancel is set to true by default.
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

        sink.tryEmitNext("Foo");

        StepVerifier.create(sink.asFlux()).expectNext("Foo").thenCancel().verify();

        // After the first subscriber cancels, the sink shuts down unless autoCancel is false.
        StepVerifier.create(sink.asFlux()).expectComplete().verify();
    }

    @Test
    void onBackpressureBufferDoesReplaysOnlyToFirstSubscriber() {
        boolean autoCancel = false;
        Sinks.Many<String> sink = Sinks
                .many()
                .multicast()
                .onBackpressureBuffer(SMALL_BUFFER_SIZE, autoCancel);

        sink.tryEmitNext("Foo");

        // The sink's buffer fills up until the first subscription.
        // As a result, the first subscriber should receive the signal.
        StepVerifier.create(sink.asFlux()).expectNext("Foo").thenCancel().verify();

        StepVerifier stepVerifier = StepVerifier
                .create(sink.asFlux())
                .expectNext("Bar")
                .thenCancel()
                .verifyLater();

        sink.tryEmitNext("Bar");

        stepVerifier.verify();
    }
}