package org.rh.example.api;

import org.rh.example.executor.LoggingSchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class LoggerController {

    @Autowired
    LoggingSchedulerUtils loggingSchedulerUtils;

    private static final Logger logger = LoggerFactory.getLogger(LoggerController.class);

    /**
     * EXAMPLE: curl 'http://localhost:8080/logs/ping'
     *
     * @return
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public ResponseEntity<Object> ping() {
        return new ResponseEntity<>("Pong", HttpStatus.OK);
    }

    /**
     * EXAMPLE: curl POST 'http://localhost:8080/logs/test'
     *
     * @return
     */
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public ResponseEntity<Object> logMessage() {
        Date date = new Date();
        logger.info("Ping from /logger/test on thread '{}' @ {}", Thread.currentThread().getName(), date);
        return new ResponseEntity<>("Test Message @ " + date, HttpStatus.CREATED);
    }

    /**
     * EXAMPLE: curl POST 'http://localhost:8080/logs/generate?runtime=60'
     * EXAMPLE: curl POST 'http://localhost:8080/logs/generate?runtime=60&polltime=1&message_length=200
     *
     * @param runtime
     * @param polltime
     * @param logMessageLength
     * @return
     */
    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public ResponseEntity<Object> logGenerator(
            @RequestParam(value = "runtime", required = false, defaultValue = "60") int runtime,
            @RequestParam(value = "polltime", required = false, defaultValue = "5") int polltime,
            @RequestParam(value = "message_length", required = false, defaultValue = "0") int logMessageLength
    ) {
        logger.info("Running Logging Generator for '{}' Seconds, Polling at an interval of '{}' " +
                "with a specified log message length of : '{}'", runtime, polltime, logMessageLength);

        boolean logRun = loggingSchedulerUtils.scheduleLoggingGeneration(runtime, polltime, logMessageLength);

        if (!logRun) {
            return new ResponseEntity<>("Logging Generator is already running, please try again later...!",
                    HttpStatus.METHOD_NOT_ALLOWED);
        }

        return new ResponseEntity<>("Logging Generator scheduled to run for '" + runtime + "' Seconds",
                HttpStatus.CREATED);
    }


}
