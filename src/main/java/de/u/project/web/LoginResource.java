package de.u.project.web;

import java.net.URI;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class LoginResource {

    /** Default Quarkus form-auth session cookie name. */
    private static final String CREDENTIAL_COOKIE = "quarkus-credential";

    @Inject
    Template login;

    @GET
    @Path("/login")
    public TemplateInstance loginPage(@RestQuery String error) {
        return login.data("error", error != null);
    }

    @POST
    @Path("/logout")
    public Response logout() {
        NewCookie expired = new NewCookie.Builder(CREDENTIAL_COOKIE)
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        return Response.seeOther(URI.create("/")).cookie(expired).build();
    }
}
