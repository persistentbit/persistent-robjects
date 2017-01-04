package com.persistentbit.substema;

import com.persistentbit.core.logging.LogPrinter;
import com.persistentbit.core.result.Result;

import java.util.concurrent.TimeUnit;

/**
 * @author Peter Muys
 * @since 22/09/2016
 */
public class RemoteServiceLogger implements RemoteService{
    private RemoteService   master;

    public RemoteServiceLogger(RemoteService master) {
        this.master = master;
    }

    @Override
    public Result<RCallResult> call(RCall call) {
        return Result.function(call).code(l -> {
            return master.call(call)
                .completed()
                .withLogs(le -> LogPrinter.consoleInColor().print(le));
        });
    }

    @Override
    public void close(long timeOut, TimeUnit timeUnit) {

    }
}
