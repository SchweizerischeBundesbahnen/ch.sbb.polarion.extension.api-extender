package ch.sbb.polarion.extension.api_extender.velocity.tools_v3;

import com.polarion.core.util.logging.Logger;
import org.apache.velocity.tools.generic.LogTool;

/**
 * LogTool writes to SLF4J, which has no provider in the isolated class loader. This tool writes to the Polarion log.
 * <p>
 * Polarion's Velocity 1.4 renders a {@code void} method call as an empty string, so a log call adds nothing to the page.
 */
public class PolarionLogTool extends LogTool {

    private static final Logger logger = Logger.getLogger(PolarionLogTool.class);

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }
}
