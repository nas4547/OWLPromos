/**
 * A custom class to log information to the console.
 *
 * @author Nate S.
 */
public class NLogger {
    private boolean withTime;

    /**
     * Constructor for NLogger
     *
     * @param timeLogging Should the current time be included in the logs?
     */
    public NLogger(boolean timeLogging) {
        this.withTime = timeLogging;
        info("Running NLogger with timeLogging = " + timeLogging);
    }

    /**
     * Gets the current system time.
     *
     * @return The system time.
     */
    public String getTime() {
        return java.util.Calendar.getInstance().getTime() + "";
    }

    /**
     * Logs a message to the console with the INFO tag.
     *
     * @param msg The message to log.
     */
    public void info(String msg) {
        if (!withTime)
            System.out.println("[INFO] " + msg);
        else
            System.out.println("[INFO @ " + getTime() + "] " + msg);
    }

    /**
     * Logs a message to the console with the WARNING tag.
     *
     * @param msg The message to log.
     */
    public void warn(String msg) {
        if (!withTime)
            System.out.println("[WARNING] " + msg);
        else
            System.out.println("[WARNING @ " + getTime() + "] " + msg);
    }

    /**
     * Logs a message to the console with the WARNING tag, with an exception printed.
     *
     * @param msg The message to log.
     */
    public void warn(String msg, Exception e) {
        if (!withTime) {
            System.out.println("[WARNING] " + msg);
            e.printStackTrace();
        } else
            System.out.println("[WARNING @ " + getTime() + "] " + msg);
    }
}