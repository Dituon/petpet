package moe.dituon.petpet.share;

import net.mamoe.mirai.utils.MiraiLogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import static java.util.logging.Level.INFO;

public class BaseLogger {
    private static class BaseLoggerInner {
        private static final BaseLogger INSTANCE = new BaseLogger();
    }

    public static BaseLogger getInstance() {
        return BaseLoggerInner.INSTANCE;
    }

    protected LoggerInterface logger = new JavaUtilLogger("Petpet");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void setLogger(LoggerInterface loggerInterface) {
        this.logger = loggerInterface;
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warning(String message) {
        logger.warning(message);
    }

    public void warning(String message, Throwable throwable) {
        logger.warning(message, throwable);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public interface LoggerInterface {
        void info(String message);

        void warning(String message);

        void warning(String message, Throwable throwable);

        void error(String message, Throwable throwable);
    }

    public static class JavaUtilLogger implements LoggerInterface {
        private final java.util.logging.Logger logger;

        public JavaUtilLogger(String name) {
            logger = java.util.logging.Logger.getLogger(name);
            logger.setUseParentHandlers(false);

            StreamHandler handler = new StreamHandler(System.out, new Formatter() {
                @Override
                public synchronized String format(LogRecord record) {
                    String formattedTime = dateTimeFormatter.format(LocalDateTime.now());
                    Level level = record.getLevel();
                    String message = record.getMessage();
                    Throwable throwable = record.getThrown();

                    StringBuilder sb = new StringBuilder();
                    sb.append(level == INFO ? "\u001B[32m" : "\u001B[33m")
                            .append("[")
                            .append(formattedTime)
                            .append("] ")
                            .append(level.getName())
                            .append(": ")
                            .append(message).append(System.lineSeparator())
                            .append("\u001B[0m");

                    if (throwable != null) {
                        sb.append(throwable).append(System.lineSeparator());
                    }

                    return sb.toString();
                }
            }) {
                @Override
                public void publish(LogRecord record) {
                    super.publish(record);
                    flush();
                }

                @Override
                public void close() {
                    flush();
                }
            };

            handler.setLevel(Level.ALL);

            logger.addHandler(handler);
        }

        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void warning(String message) {
            logger.warning(message);
        }

        @Override
        public void warning(String message, Throwable throwable) {
            logger.log(Level.WARNING, message, throwable);
        }

        @Override
        public void error(String message, Throwable throwable) {
            logger.log(Level.SEVERE, message, throwable);
        }
    }

    public static class MiraiUtilLogger implements LoggerInterface {
        private final MiraiLogger logger;

        public MiraiUtilLogger(MiraiLogger logger) {
            this.logger = logger;
        }

        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void warning(String message) {
            logger.warning(message);
        }

        @Override
        public void warning(String message, Throwable throwable) {
            logger.warning(message, throwable);
        }

        @Override
        public void error(String message, Throwable throwable) {
            logger.warning(message, throwable);
        }
    }
}
