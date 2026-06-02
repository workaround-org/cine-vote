package de.u.project.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;

/**
 * Creates the initial admin user from configuration when no admin exists yet.
 */
@ApplicationScoped
public class StartupBootstrap {

    @ConfigProperty(name = "cinevote.admin.bootstrap-username")
    String adminUsername;

    @ConfigProperty(name = "cinevote.admin.bootstrap-password")
    String adminPassword;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (AppUser.count() == 0) {
            AppUser.add(adminUsername, BcryptUtil.bcryptHash(adminPassword), "admin");
        }
    }
}
