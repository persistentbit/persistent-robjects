package com.persistentbit.substema;

import com.persistentbit.core.logging.PLog;

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

	private static final PLog log = PLog.get(RSessionManager.class);
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
		log.debug("Set Session data " + data + ", " + expires);
		this.data = data;
		if(data == null) {
			this.expires = null;
		}
		else {
			this.expires = expires;
		}
	}

	public Optional<DATA> getData() {
		return Optional.ofNullable(data);
	}

	public Optional<LocalDateTime> getExpires() {
		return Optional.of(expires);
	}
}
