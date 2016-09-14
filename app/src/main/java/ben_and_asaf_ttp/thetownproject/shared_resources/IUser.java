package ben_and_asaf_ttp.thetownproject.shared_resources;

/**
 * {@code IUser} interface, holds the methods for all the {@code User} instances
 * @author Ben Gilad and Asaf Yeshayahu
 * @version %I%
 * @see User
 * @since 1.0
 */
public interface IUser {
	/**
     * Checks equality by comparing username AND password
     *
     * @param o {@code User} object
     * @return <code>True/False</code> Equal/Different
     */
    @Override
    public abstract boolean equals(Object o);
    
    /**
     * toString method, prints information about this {@code User}
     * @return The formatted string
     */
    @Override
    public abstract String toString();
}
