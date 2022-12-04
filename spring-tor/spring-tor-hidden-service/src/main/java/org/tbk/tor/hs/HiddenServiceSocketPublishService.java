package org.tbk.tor.hs;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.Objects.requireNonNull;

@Slf4j
public class HiddenServiceSocketPublishService extends AbstractIdleService implements Publisher<Socket> {

    private final String serviceId = Integer.toHexString(System.identityHashCode(this));

    private final ExecutorService publisherExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("tor-hidden-service-pub-" + serviceId + "-%d")
            .setDaemon(false)
            .build());

    private final SubmissionPublisher<Socket> publisher = new SubmissionPublisher<>(publisherExecutor, Flow.defaultBufferSize());

    private final Scheduler subscribeOnScheduler = Schedulers.newParallel("tor-hidden-service-sub-" + serviceId);

    private final HiddenServiceSocket socket;

    private Disposable subscription;

    public HiddenServiceSocketPublishService(HiddenServiceSocket socket) {
        this.socket = requireNonNull(socket);
    }

    @Override
    protected final String serviceName() {
        return String.format("%s-%s-%s", super.serviceName(), socket.getServiceName(), serviceId);
    }

    @Override
    public void subscribe(Subscriber<? super Socket> s) {
        publisher.subscribe(FlowAdapters.toFlowSubscriber(s));
    }

    @Override
    protected final void startUp() {
        log.info("starting..");

        Flux<Socket> socketFlux = createSocketFlux();

        this.subscription = socketFlux
                .onErrorContinue((error, obj) -> {
                    log.error("Error on subscription value", error);
                })
                .parallel()
                .runOn(subscribeOnScheduler)
                .doOnNext(publisher::submit)
                .subscribe(it -> {
                    try {
                        it.close();
                    } catch (final IOException e) {
                        // there is nothing we can really do on errors when closing sockets
                        if (log.isDebugEnabled()) {
                            log.debug("Error while closing socket", e);
                        }
                    }
                });

        log.info("started successfully");
    }


    private Flux<Socket> createSocketFlux() {
        return Flux.create(new Consumer<FluxSink<Socket>>() {
            @SuppressFBWarnings(
                    value = "SECCRLFLOG",
                    justification = "It's acceptable to log InetAddress in trace mode."
            )
            @Override
            public void accept(FluxSink<Socket> fluxSink) {
                while (!socket.isClosed()) {
                    try {
                        Socket acceptedSocket = socket.accept();
                        if (log.isTraceEnabled()) {
                            log.trace("socket accepted: {}", acceptedSocket.getInetAddress());
                        }

                        fluxSink.next(acceptedSocket);
                    } catch (IOException e) {
                        fluxSink.error(e);
                    }
                }
            }
        });
    }

    @Override
    protected final void shutDown() {
        log.info("terminating..");

        this.subscription.dispose();
        this.subscribeOnScheduler.dispose();

        this.publisher.close();

        boolean executorShutdownSuccessful = shutdownAndAwaitTermination(publisherExecutor, Duration.ofSeconds(10));
        if (!executorShutdownSuccessful) {
            log.warn("unclean shutdown of executor service");
        }

        log.info("terminated");
    }
}
