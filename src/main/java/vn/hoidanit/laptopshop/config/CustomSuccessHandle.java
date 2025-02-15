package vn.hoidanit.laptopshop.config;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UserService;

public class CustomSuccessHandle implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    /**
     * 
     * @param authentication: user information is saved after login successful
     * @return determineTargetUrl: Defines the URL the user will be redirected to
     *         after successful login, based on the user's role.
     *         - If the user's permission (role) matches one of the keys in the
     *         roleTargetUrlMap, the corresponding URL is returned.
     *         - If no valid role is found, an IllegalStateException is thrown.
     */
    protected String determineTargetUrl(final Authentication authentication) {

        Map<String, String> roleTargetUrlMap = new HashMap<>();
        roleTargetUrlMap.put("ROLE_USER", "/");
        roleTargetUrlMap.put("ROLE_ADMIN", "/admin");

        // Get user role via authentication
        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Loop through authorities -> check authorities exist on roleTargetUrlMap ->
        // get url
        for (final GrantedAuthority grantedAuthority : authorities) {
            String authorityName = grantedAuthority.getAuthority();
            if (roleTargetUrlMap.containsKey(authorityName)) {
                return roleTargetUrlMap.get(authorityName);
            }
        }

        // return exception if no role is found
        throw new IllegalStateException();
    }

    /**
     * 
     * @param request:        HttpServletRequest
     * @param authentication: user information is saved after login successful
     */
    protected void clearAuthenticationAttributes(HttpServletRequest request, Authentication authentication) {
        // - if there is no current session, return null, instead of creating a new
        // session if there is not one.
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        // Get email via authentication (email is used to login)
        String email = authentication.getName();

        // Get information user login
        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            // Save session object
            session.setAttribute("user", user);
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("avatar", user.getAvatar());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("id", user.getId());

            int sum = user.getCart() == null ? 0 : user.getCart().getSum();
            session.setAttribute("sum", sum);
        }

    }

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            // Todo: Logging, Handle Exception
            return;
        }

        // Redirect to url via role authorization
        redirectStrategy.sendRedirect(request, response, targetUrl);
        clearAuthenticationAttributes(request, authentication);

    }

}
