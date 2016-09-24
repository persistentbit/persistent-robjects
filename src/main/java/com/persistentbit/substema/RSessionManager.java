package com.persistentbit.substema;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by petermuys on 18/09/16.
 */
public class RSessionManager<DATA> {
    private DATA data;
    private LocalDateTime   expires;

    public RSessionManager(DATA data,LocalDateTime expires) {
        this.data = data;
        this.expires = expires;
    }

    public RSessionManager() {
        this(null,null);
    }



    public void setData(DATA data, LocalDateTime expires){
        System.out.println("Set Session data " + data + ", " + expires);
        this.data = data;
        if(data == null) {
            this.expires = null;
        } else {
            this.expires = expires;
        }
    }
    public Optional<DATA> getData(){
        return Optional.ofNullable(data);
    }

    public Optional<LocalDateTime>    getExpires() {
        return Optional.of(expires);
    }
}
