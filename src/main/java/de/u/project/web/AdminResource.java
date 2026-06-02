package de.u.project.web;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import de.u.project.voting.VotingQueryService;
import de.u.project.voting.VotingService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/admin")
@RolesAllowed("admin")
@Produces(MediaType.TEXT_HTML)
public class AdminResource {

    @Inject
    Template adminDashboard;

    @Inject
    VotingService votingService;

    @Inject
    VotingQueryService queryService;

    @GET
    public TemplateInstance dashboard(@RestQuery String error) {
        return adminDashboard.data("cards", queryService.cards()).data("error", error);
    }

    @POST
    @Path("/votings")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response create(@RestForm String title, @RestForm String description) {
        try {
            votingService.create(title, description);
            return redirect(null);
        } catch (IllegalArgumentException e) {
            return redirect(e.getMessage());
        }
    }

    @POST
    @Path("/votings/{id}/close")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response close(@PathParam("id") UUID id) {
        try {
            votingService.end(id);
            return redirect(null);
        } catch (IllegalStateException e) {
            return redirect(e.getMessage());
        }
    }

    private Response redirect(String error) {
        String location = "/admin";
        if (error != null) {
            location += "?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
        }
        return Response.seeOther(URI.create(location)).build();
    }
}
