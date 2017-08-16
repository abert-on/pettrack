package pettrack.domain;

import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

public class User {

    @Id
    public String id;

    public String username;
    public String password;
    public List<GrantedAuthority> grantedAuthorities;

    public User(final String username, final String password, final String[] authorities) {
        this.username = username;
        this.password = password;
        this.grantedAuthorities = AuthorityUtils.createAuthorityList(authorities);
    }
}
