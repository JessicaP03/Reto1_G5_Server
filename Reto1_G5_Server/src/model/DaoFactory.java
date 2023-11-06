
package model;

/**
 * Esta clase es una factoria de la implementación de la interfaz.
 * @author Jessica.
 */
public class DaoFactory {

    public static Signable getDao() {
        return new DaoImplementation();
    }
}
