package model;

/**
 * Esta clase es una factoria de Pools que al mismo tiempo la convierte en
 * Singelton
 *
 * @author Jason.
 */
public class PoolFactory {

    private static Pool pool;

    /**
     * Este metodo devuelve el mismo pool cada vez que se le pida.
     *
     * @return pool un pool.
     */
    public static Pool getPool() {
        if (pool == null) {
            pool = new Pool();
        }

        return pool;
    }
}
