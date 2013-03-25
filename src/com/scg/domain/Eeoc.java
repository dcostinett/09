package com.scg.domain;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 2/2/13
 * Time: 1:09 PM
 * The EEOC monitors SCG's terminations.
 */
public class Eeoc implements TerminationListener {

    private int forcedTerminationCount = 0;
    private int voluntaryTerminationCount = 0;

    private static final Logger LOGGER = Logger.getLogger("Eeoc.class");
    /**
     * Simply prints a message indicating that the consultant quit.
     * @param evt - the termination event
     */
    @Override
    public void voluntaryTermination(final TerminationEvent evt) {
        voluntaryTerminationCount++;
        final Consultant consultant = evt.getConsultant();
        LOGGER.info(String.format("%s quit.", consultant.getName()));
    }

    /**
     * Simply prints a message indicating that the consultant was fired.
     * @param evt - the termination event
     */
    @Override
    public void forcedTermination(final TerminationEvent evt) {
        forcedTerminationCount++;
        final Consultant consultant = evt.getConsultant();
        LOGGER.info(String.format("%s was fired.", consultant.getName()));
    }

    /**
     * Gets the forced termination count.
     * @return number of firings
     */
    public int forcedTerminationCount() {
        return forcedTerminationCount;
    }

    /**
     * Gets the voluntary termination count.
     * @return number of employee resignations
     */
    public int voluntaryTerminationCount() {
        return voluntaryTerminationCount;
    }
}
