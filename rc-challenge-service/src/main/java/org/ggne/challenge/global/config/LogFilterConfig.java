package org.ggne.challenge.global.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogFilterConfig {

    @PostConstruct
    public void suppressOutboxPollingLogs() {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

        ctx.addTurboFilter(new TurboFilter() {
            @Override
            public FilterReply decide(Marker marker, Logger logger, Level level,
                                      String format, Object[] params, Throwable t) {

                if ("org.hibernate.SQL".equals(logger.getName())
                        && format != null && format.contains("outbox_events")) {
                    return FilterReply.DENY;
                }

                return FilterReply.NEUTRAL;
            }
        });
    }
}
