package net.anotheria.rproxy.refactor;

import org.configureme.annotations.ConfigureMe;

/**
 * Site Credentials class.
 */
@ConfigureMe(allfields = true)
public class SiteCredentials {
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "SiteCredentials{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
