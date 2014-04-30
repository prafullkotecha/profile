/*
 * Copyright (C) 2007-2014 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.security.authentication.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.craftercms.profile.api.Profile;
import org.craftercms.profile.api.Ticket;
import org.craftercms.profile.api.exceptions.ErrorCode;
import org.craftercms.profile.api.exceptions.ProfileException;
import org.craftercms.profile.api.services.AuthenticationService;
import org.craftercms.profile.api.services.ProfileService;
import org.craftercms.profile.v2.exceptions.ProfileRestServiceException;
import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.authentication.AuthenticationManager;
import org.craftercms.security.exception.AuthenticationException;
import org.craftercms.security.exception.AuthenticationSystemException;
import org.craftercms.security.exception.BadCredentialsException;
import org.craftercms.security.exception.DisabledUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link org.craftercms.security.authentication.AuthenticationManager}.
 *
 * @author avasquez
 */
public class AuthenticationManagerImpl implements AuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManagerImpl.class);

    protected AuthenticationService authenticationService;
    protected ProfileService profileService;
    protected Cache authenticationCache;

    @Required
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Required
    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Required
    public void setAuthenticationCache(Cache authenticationCache) {
        this.authenticationCache = authenticationCache;
    }

    @Override
    public Authentication authenticateUser(String tenant, String username, String password) {
        try {
            Ticket ticket = authenticationService.authenticate(tenant, username, password);
            Profile profile = profileService.getProfile(ticket.getProfileId());
            String ticketId = ticket.getId().toString();
            DefaultAuthentication auth = new DefaultAuthentication(ticketId, profile);

            putAuthenticationInCache(ticketId, auth);

            logger.debug("Authentication successful for user '{}' (ticket ID = '{}')", username, ticketId);

            return auth;
        } catch (ProfileRestServiceException e) {
            switch (e.getErrorCode()) {
                case DISABLED_PROFILE:
                    throw new DisabledUserException("User '" + username + "' is disabled", e);
                case BAD_CREDENTIALS:
                    throw new BadCredentialsException("Invalid username or password", e);
                default:
                    throw new AuthenticationSystemException("An unexpected error occurred while attempting " +
                            "authentication for user '" + username + "'", e);
            }
        } catch (ProfileException e) {
            throw new AuthenticationSystemException("An unexpected error occurred while attempting authentication " +
                    "for user '" + username + "'", e);
        }
    }

    @Override
    public Authentication getAuthentication(String ticket, boolean reloadProfile) throws AuthenticationException {
        DefaultAuthentication auth = getCachedAuthentication(ticket);
        if (auth == null || reloadProfile) {
            if (reloadProfile) {
                logger.debug("Profile reload forced for ticket '{}'", ticket);
            } else {
                logger.debug("Ticket '{}' found in request but there's no cached authentication for it", ticket);
            }

            Profile profile = loadProfile(ticket);
            if (profile != null) {
                auth = new DefaultAuthentication(ticket, profile);

                putAuthenticationInCache(ticket, auth);
            } else {
                return null;
            }
        }

        return auth;
    }

    @Override
    public void invalidateAuthentication(Authentication authentication) {
        try {
            removeAuthenticationFromCache(authentication.getTicket());

            authenticationService.invalidateTicket(authentication.getTicket());

            logger.debug("Ticket '{}' successfully invalidated");
        } catch (ProfileException e) {
            throw new AuthenticationSystemException("An unexpected error occurred while attempting to invalidate " +
                    "ticket '" + authentication.getTicket() + "'", e);
        }
    }

    protected Profile loadProfile(String ticketId) throws AuthenticationException {
        try {
            Profile profile = profileService.getProfileByTicket(ticketId);
            if (profile != null) {
                logger.debug("Profile '{}' retrieved for ticket '{}'", profile.getId(), ticketId);

                return profile;
            } else {
                throw new AuthenticationSystemException("No profile found for valid ticket '" + ticketId + "'");
            }
        } catch (ProfileRestServiceException e) {
            if (e.getErrorCode() == ErrorCode.NO_SUCH_TICKET) {
                logger.debug("Ticket '{}' is invalid", ticketId);

                return null;
            } else {
                throw new AuthenticationSystemException("An unexpected error occurred while attempting to retrieve " +
                        "profile for ticket '" + ticketId + "'", e);
            }
        } catch (ProfileException e) {
            throw new AuthenticationSystemException("An unexpected error occurred while attempting to retrieve " +
                    "profile for ticket '" + ticketId + "'", e);
        }
    }

    protected DefaultAuthentication getCachedAuthentication(String ticket) {
        Element element = authenticationCache.get(ticket);
        if (element != null) {
            return (DefaultAuthentication) element.getObjectValue();
        } else {
            return null;
        }
    }

    protected void putAuthenticationInCache(String ticket, DefaultAuthentication authentication) {
        authenticationCache.put(new Element(ticket, authentication));
    }

    protected void removeAuthenticationFromCache(String ticket) {
        authenticationCache.remove(ticket);
    }

}
