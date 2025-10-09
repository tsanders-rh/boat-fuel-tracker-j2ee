package com.boatfuel.resource;

import com.boatfuel.entity.FuelUp;
import com.boatfuel.service.FuelUpService;
import com.boatfuel.service.FuelUpStatistics;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDate;
import java.util.List;

/**
 * JAX-RS Resource replacing the legacy servlet
 */
@Path("/fuelups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"user", "admin"})
public class FuelUpResource {

    @Inject
    FuelUpService fuelUpService;

    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Get all fuel-ups for a user
     */
    @GET
    @Path("/user/{userId}")
    public List<FuelUp> getFuelUpsByUser(@PathParam("userId") String userId) {
        String authenticatedUser = securityIdentity.getPrincipal().getName();

        // Users can only see their own data (unless admin)
        if (!securityIdentity.hasRole("admin") && !authenticatedUser.equals(userId)) {
            throw new ForbiddenException("You can only view your own fuel-ups");
        }

        Log.infof("User %s accessed fuel-ups", userId);
        return fuelUpService.getFuelUpsByUser(userId);
    }

    /**
     * Get fuel-ups for a user within a date range
     */
    @GET
    @Path("/user/{userId}/range")
    public List<FuelUp> getFuelUpsByUserAndDateRange(
            @PathParam("userId") String userId,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return fuelUpService.getFuelUpsByUserBetweenDates(userId, start, end);
    }

    /**
     * Create a new fuel-up
     */
    @POST
    public Response createFuelUp(FuelUp fuelUp) {
        Log.infof("Creating fuel-up for user: %s", fuelUp.user.userId);
        FuelUp created = fuelUpService.createFuelUp(fuelUp);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Delete a fuel-up
     */
    @DELETE
    @Path("/{id}")
    public Response deleteFuelUp(@PathParam("id") Long id) {
        Log.infof("Deleting fuel-up: %d", id);
        fuelUpService.deleteFuelUp(id);
        return Response.noContent().build();
    }

    /**
     * Get statistics for a user
     */
    @GET
    @Path("/user/{userId}/statistics")
    public FuelUpStatistics getStatistics(@PathParam("userId") String userId) {
        Log.infof("Getting statistics for user: %s", userId);
        return fuelUpService.getStatistics(userId);
    }
}
