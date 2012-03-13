/*
 * Created on Oct 20, 2004
 */
package com.devoteam.srit.xmlloader.core.log;

public interface TextListenerProvider
{
    /**
     * This interface defines a TextListener provider. This class returns an
     * implementation of the TextListener interface.
     *
     * The returned TextListener has to be coherent towards the passed key.
     *
     * This key can take the following values:
     *   instance of a ScenarioRunner
     *  (instance of a TestcaseRunner)
     *   null : application (global) logger
     * @param key
     * @return
     */
    public TextListener provide(TextListenerKey key);


    /**
     * Free resources used by the TextListener disigned by the "key".
     * @param key
     */
    public void dispose(TextListenerKey key);
}
