package com.persistentbit.substema;

import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple2;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Peter Muys
 * @since 22/09/2016
 */
public class RemoteServiceLogger implements RemoteService{
    private       RemoteService                     master;
    private final Consumer<Tuple2<RCall, LogEntry>> effect;

    public RemoteServiceLogger(RemoteService master, Consumer<Tuple2<RCall, LogEntry>> effect) {
        this.master = master;
        this.effect = effect;
    }

    @Override
    public Result<RCallResult> call(RCall call) {
        return Result.function(call).code(l -> {
            return master.call(call)
                .completed()
                .doWithLogs(le -> effect.accept(Tuple2.of(call, le)));
        });
    }

    @Override
    public void close(long timeOut, TimeUnit timeUnit) {

    }
}
