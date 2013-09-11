/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.profile.services.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.craftercms.profile.domain.Profile;
import org.craftercms.profile.domain.Ticket;
import org.craftercms.profile.exceptions.InvalidEmailException;
import org.craftercms.profile.repositories.ProfileRepository;
import org.craftercms.profile.repositories.TicketRepository;
import org.craftercms.profile.services.EmailValidatorService;
import org.craftercms.profile.services.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ProfileServiceImpl implements ProfileService {

    private final transient Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EmailValidatorService emailValidatorService;
    
    private List<String> protectedDisableUsers;

    @Override
    public Profile createProfile(String userName, String password, Boolean active, String tenantName, String email,
                                 Map<String, Serializable> attributes, List<String> roles,
                                 HttpServletResponse response) throws InvalidEmailException {
        if (!emailValidatorService.validateEmail(email)) {
            throw new InvalidEmailException("Invalid email account format");
        }
        PasswordEncoder encoder = new Md5PasswordEncoder();
        String hashedPassword = encoder.encodePassword(password, null);
        Profile profile = new Profile();
        profile.setUserName(userName);
        profile.setPassword(hashedPassword);
        if (!isProtectedToKeepActive(userName)) {
        	profile.setActive(active);
        } else {
        	profile.setActive(true);
        }
        profile.setTenantName(tenantName);
        profile.setCreated(new Date());
        profile.setModified(new Date());
        profile.setAttributes(attributes);
        profile.setEmail(email);
        profile.setRoles(roles);
        try {
            return profileRepository.save(profile);
        } catch (DuplicateKeyException e) {
            try {
                if (response != null) {
                    response.sendError(HttpServletResponse.SC_CONFLICT);
                }
            } catch (IOException e1) {
                log.error("Can't set error status after a DuplicateKey exception was received.");
            }
        }
        return null;
    }

    @Override
    public List<Profile> getProfileRange(String tenantName, String sortBy, String sortOrder,
                                         List<String> attributesList, int start, int end) {
        return profileRepository.getProfileRange(tenantName, sortBy, sortOrder, attributesList, start, end);
    }

    @Override
    public long getProfilesCount(String tenantName) {
        return profileRepository.getProfilesCount(tenantName);
    }


    @Override
    public Profile updateProfile(String profileId, String userName, String password, Boolean active,
                                 String tenantName, String email, Map<String, Serializable> attributes,
                                 List<String> roles) {
        Profile profile = profileRepository.findOne(new ObjectId(profileId));

        if (profile == null) {
            return profile;
        }
        if (userName != null && !userName.trim().isEmpty()) {
            profile.setUserName(userName);
        }

        if (password != null && !password.trim().isEmpty()) {
            PasswordEncoder encoder = new Md5PasswordEncoder();
            String hashedPassword = encoder.encodePassword(password, null);
            profile.setPassword(hashedPassword);
        }

        if (active != null && !isProtectedToKeepActive(userName)) {
            profile.setActive(active);
        }

        if (tenantName != null && !tenantName.trim().isEmpty()) {
            profile.setTenantName(tenantName);
        }

        if (roles != null) {
            profile.setRoles(roles);
        }

        if (email != null) {
            profile.setEmail(email);
        }
        Map<String, Serializable> currentAttributes = profile.getAttributes();
        if (currentAttributes != null && attributes != null) {
            currentAttributes.putAll(attributes);
        } else {
            currentAttributes = attributes;
        }

        profile.setAttributes(currentAttributes);
        profile.setModified(new Date());
        profile = profileRepository.save(profile);
        return profile;
    }

    @Override
    public Profile getProfileByTicket(String ticketStr) {
        Ticket ticket = ticketRepository.getByTicket(ticketStr);
        if (ticket == null) {
            return null;
        }
        return getProfileByUserName(ticket.getUsername(), ticket.getTenantName(), null);
    }

    @Override
    public Profile getProfileByTicket(String ticketStr, List<String> attributes) {
        Ticket ticket = ticketRepository.getByTicket(ticketStr);
        if (ticket == null) {
            return null;
        }
        return getProfileByUserName(ticket.getUsername(), ticket.getTenantName(), attributes);
    }

    @Override
    public Profile getProfileByTicketWithAllAttributes(String ticketString) {
        Ticket ticket = ticketRepository.getByTicket(ticketString);
        if (ticket == null) {
            return null;
        }
        return getProfileByUserNameWithAllAttributes(ticket.getUsername(), ticket.getTenantName());
    }

    @Override
    public Profile getProfile(String profileId) {
        return profileRepository.getProfile(profileId);
    }

    @Override
    public Profile getProfile(String profileId, List<String> attributes) {
        return profileRepository.getProfile(profileId, attributes);
    }

    @Override
    public Profile getProfileWithAllAttributes(String profileId) {
        return profileRepository.findOne(new ObjectId(profileId));
    }

    @Override
    public Profile getProfileByUserName(String userName, String tenantName) {
        return profileRepository.getProfileByUserName(userName, tenantName);
    }

    @Override
    public Profile getProfileByUserName(String userName, String tenantName, List<String> attributes) {
        return profileRepository.getProfileByUserName(userName, tenantName, attributes);
    }

    @Override
    public Profile getProfileByUserNameWithAllAttributes(String userName, String tenantName) {
        return profileRepository.getProfileByUserNameWithAllAttributes(userName, tenantName);
    }

    @Override
    public List<Profile> getProfiles(List<String> profileIdList) {
        return profileRepository.getProfiles(profileIdList);
    }

    @Override
    public List<Profile> getProfilesWithAttributes(List<String> profileIdList) {
        return profileRepository.getProfilesWithAttributes(profileIdList);
    }

    @Override
    public void activeProfile(String profileId, boolean active) {
        Profile p = profileRepository.findOne(new ObjectId(profileId));
        if (p != null) {
            activeProfile(p, active);
        }

    }

    @Override
    public void activeProfiles(boolean active) {
        List<Profile> l = profileRepository.findAll();
        for (Profile p : l) {
            activeProfile(p, active);
        }
    }

    private void activeProfile(Profile p, boolean active) {
        p.setActive(active);
        profileRepository.save(p);
    }

    @Override
    public void deleteProfiles(String tenantName) {
        profileRepository.delete(getProfilesByTenant(tenantName));
    }

    public List<Profile> getProfilesByRoleName(String roleName, String tenantName) {
        return profileRepository.findByRolesAndTenantName(roleName, tenantName);
    }

    private List<Profile> getProfilesByTenant(String tenantName) {
        return profileRepository.getProfilesByTenantName(tenantName);
    }

    @Override
    public void setAttributes(String profileId, Map<String, Serializable> attributes) {
        profileRepository.setAttributes(profileId, attributes);
    }

    @Override
    public Map<String, Serializable> getAllAttributes(String profileId) {
        return profileRepository.getAllAttributes(profileId);
    }

    @Override
    public Map<String, Serializable> getAttributes(String profileId, List<String> attributes) {
        return profileRepository.getAttributes(profileId, attributes);
    }

    @Override
    public Map<String, Serializable> getAttribute(String profileId, String attributeKey) {
        return profileRepository.getAttribute(profileId, attributeKey);
    }

    @Override
    public void deleteAllAttributes(String profileId) {
        profileRepository.deleteAllAttributes(profileId);
    }

    @Override
    public void deleteAttributes(String profileId, List<String> attributes) {
        profileRepository.deleteAttributes(profileId, attributes);
    }
    
    @Value("#{ssrSettings['protected-disabled-users']}")
    public void setProtectedDisableUsers(String users) {
        this.protectedDisableUsers = convertLineToList(users);

    }
    
    private List<String> convertLineToList(String list) {
        List<String> values = new ArrayList<String>();
        if (list == null || list.length() == 0) {
            return values;
        }
        String[] arrayRoles = list.split(",");
        for (String role : arrayRoles) {
            values.add(role.trim());
        }
        return values;
    }
    
    private boolean isProtectedToKeepActive(String username) {
    	boolean protectedUsername = false;
    	if (this.protectedDisableUsers == null || this.protectedDisableUsers.size() == 0) {
    		return protectedUsername;
    	}
    	for(String u: protectedDisableUsers) {
    		if (u.equals(username)) {
    			protectedUsername = true;
    		}
    	}
    	return protectedUsername;
    }

}