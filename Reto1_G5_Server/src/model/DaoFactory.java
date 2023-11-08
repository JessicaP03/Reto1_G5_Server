package model;

/**
 * Esta clase es una factoria de la implementación de la interfaz.
 *
 * @author Jessica.
 */
public class DaoFactory {

    /**
     * Devuelve la implementacion de la interfaz.
     *
     * @return la implementacion de la interfaz.
     */
    public static Signable getDao() {
        return new DaoImplementation();
    }
}
