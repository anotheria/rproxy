package net.anotheria.rproxy.conf;

/**
 * Credentials entity. Username and Password for specific URL.
 */
public class Credentials {

    private String userName;
    private String password;
    private int linkNum;

    public Credentials(String userName, String password, int linkNum) {
        this.userName = userName;
        this.password = password;
        this.linkNum = linkNum;
    }

    public Credentials() {
    }

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

    public int getLinkNum() {
        return linkNum;
    }

    public void setLinkNum(int linkNum) {
        this.linkNum = linkNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credentials that = (Credentials) o;

        if (linkNum != that.linkNum) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        return password != null ? password.equals(that.password) : that.password == null;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + linkNum;
        return result;
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", linkNum=" + linkNum +
                '}';
    }
}
