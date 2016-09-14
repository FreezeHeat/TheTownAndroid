package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * {@code User} <i>abstract</i> class, holds the information about each user in the database
 * @author Ben Gilad and Asaf Yeshayahu
 * @version %I%
 * @since 1.0
 */
@MappedSuperclass
public abstract class User implements Serializable, IUser{
	
    /**
     * The user's username
     * <p><b>Note:</b> PK in the database, so it's unique</p>
     */
	@Id
	@Column(name="Username", length=30)
    protected String username;
    
    /**
     * The user's password
     */
	@Column(name="Password", length=30, nullable=false)
	protected String password;
    
    /**
     * This constructor creates an empty <code>User</code>
     */
    public User() {
    }

    /**
     * Gets the username
     *
     * @return username
     */
    public abstract String getUsername();

    /**
     * Sets the username
     *
     * @param username Username to set
     */
    public abstract void setUsername(String username);

    /**
     * Gets the password
     *
     * @return Password
     */
    public abstract String getPassword();

    /**
     * Sets the password
     *
     * @param password Password to be set
     */
    public abstract void setPassword(String password);
}
