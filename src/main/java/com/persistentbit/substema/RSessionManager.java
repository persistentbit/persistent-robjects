package com.persistentbit.substema;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.logging.Log;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A Session Manager is normally given to a service implementation and used
 * to store session data with an expiration date.<br>
 * @author Peter Muys
 * @since 18/09/16
 * @param <DATA> The Session Data type.
 */
public class RSessionManager<DATA>{

	private DATA          data;
	private LocalDateTime expires;

	public RSessionManager(DATA data, LocalDateTime expires) {
		this.data = data;
		this.expires = expires;
	}

	public RSessionManager() {
		this(null, null);
	}


	public void setData(DATA data, LocalDateTime expires) {
		Log.function(data, expires).code(l -> {
			l.info("Set Session data " + data + ", " + expires);
			this.data = data;
			if(data == null) {
				this.expires = null;
			}
			else {
				this.expires = expires;
			}
			return Nothing.inst;
		});

	}

	public Optional<DATA> getData() {
		return Optional.ofNullable(data);
	}

	public Optional<LocalDateTime> getExpires() {
		return Optional.of(expires);
	}
}
