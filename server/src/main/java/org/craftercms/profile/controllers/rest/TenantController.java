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
package org.craftercms.profile.controllers.rest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.craftercms.profile.api.AttributeDefinition;
import org.craftercms.profile.api.Tenant;
import org.craftercms.profile.api.exceptions.ProfileException;
import org.craftercms.profile.api.services.TenantService;
import org.craftercms.profile.exceptions.NoSuchTenantException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.craftercms.profile.api.ProfileConstants.BASE_URL_TENANT;
import static org.craftercms.profile.api.ProfileConstants.PARAM_ATTRIBUTE_NAME;
import static org.craftercms.profile.api.ProfileConstants.PARAM_ROLE;
import static org.craftercms.profile.api.ProfileConstants.PARAM_VERIFY;
import static org.craftercms.profile.api.ProfileConstants.PATH_VAR_NAME;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_ADD_ATTRIBUTE_DEFINITIONS;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_ADD_ROLES;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_COUNT;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_CREATE;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_DELETE;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_GET;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_GET_ALL;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_REMOVE_ATTRIBUTE_DEFINITIONS;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_REMOVE_ROLES;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_UPDATE;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_UPDATE_ATTRIBUTE_DEFINITIONS;
import static org.craftercms.profile.api.ProfileConstants.URL_TENANT_VERIFY_NEW_PROFILES;

/**
 * REST controller for the tenant service.
 *
 * @author avasquez
 */
@Controller
@RequestMapping(BASE_URL_TENANT)
@Api(value = "tenant", basePath = BASE_URL_TENANT, description = "Tenant operations")
public class TenantController {

    private TenantService tenantService;

    @Required
    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @ApiOperation(value = "Creates the given tenant", notes = "The method will fail if there's already a tenant " +
        "with the given name")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_CREATE, method = RequestMethod.POST)
    @ResponseBody
    public Tenant createTenant(@ApiParam("The tenant to create")
                               @RequestBody Tenant tenant) throws ProfileException {
        return tenantService.createTenant(tenant);
    }

    @ApiOperation(value = "Returns a tenant")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_GET, method = RequestMethod.GET)
    @ResponseBody
    public Tenant getTenant(@ApiParam("The tenant's name")
                            @PathVariable(PATH_VAR_NAME) String name) throws ProfileException {
        Tenant tenant = tenantService.getTenant(name);
        if (tenant != null) {
            return tenant;
        } else {
            throw new NoSuchTenantException(name);
        }
    }

    @ApiOperation(value = "Updates the given tenant")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_UPDATE, method = RequestMethod.POST)
    @ResponseBody
    public Tenant updateTenant(@ApiParam("The tenant to update")
                               @RequestBody Tenant tenant) throws ProfileException {
        return tenantService.updateTenant(tenant);
    }

    @ApiOperation(value = "Deletes a tenant")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_DELETE, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void deleteTenant(@ApiParam("The tenant's name")
                             @PathVariable(PATH_VAR_NAME) String name) throws ProfileException {
        tenantService.deleteTenant(name);
    }

    @ApiOperation(value = "Returns the total number of tenants")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_COUNT, method = RequestMethod.GET)
    @ResponseBody
    public long getTenantCount() throws ProfileException {
        return tenantService.getTenantCount();
    }

    @ApiOperation(value = "Returns a list with all the tenants")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_GET_ALL, method = RequestMethod.GET)
    @ResponseBody
    public List<Tenant> getAllTenants() throws ProfileException {
        List<Tenant> tenants = tenantService.getAllTenants();
        if (tenants != null) {
            return tenants;
        } else {
            return Collections.emptyList();
        }
    }

    @ApiOperation(value = "Sets if new profiles for the specified tenant should be verified or not")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_VERIFY_NEW_PROFILES, method = RequestMethod.POST)
    @ResponseBody
    public Tenant verifyNewProfiles(@ApiParam("The tenant's name")
                                    @PathVariable(PATH_VAR_NAME) String tenantName,
                                    @ApiParam("True to verify new profiles through email, false otherwise")
                                    @RequestParam(PARAM_VERIFY) boolean verify) throws ProfileException {
        return tenantService.verifyNewProfiles(tenantName, verify);
    }

    @ApiOperation(value = "Adds the given roles to the specified tenant")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_ADD_ROLES, method = RequestMethod.POST)
    @ResponseBody
    public Tenant addRoles(@ApiParam("The tenant's name")
                           @PathVariable(PATH_VAR_NAME) String tenantName,
                           @ApiParam("The roles to add")
                           @RequestParam(PARAM_ROLE) Collection<String> roles) throws ProfileException {
        return tenantService.addRoles(tenantName, roles);
    }

    @ApiOperation(value = "Removes the given roles from the specified tenant")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_REMOVE_ROLES, method = RequestMethod.POST)
    @ResponseBody
    public Tenant removeRoles(@ApiParam("The tenant's name")
                              @PathVariable(PATH_VAR_NAME) String tenantName,
                              @ApiParam("The roles to remove")
                              @RequestParam(PARAM_ROLE) Collection<String> roles) throws ProfileException {
        return tenantService.removeRoles(tenantName, roles);
    }

    @ApiOperation(value = "Adds the given attribute definitions to the specified tenant")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_ADD_ATTRIBUTE_DEFINITIONS, method = RequestMethod.POST)
    @ResponseBody
    public Tenant addAttributeDefinitions(@ApiParam("The tenant's name")
                                          @PathVariable(PATH_VAR_NAME) String tenantName,
                                          @ApiParam("The definitions to add")
                                          @RequestBody Collection<AttributeDefinition> attributeDefinitions)
            throws ProfileException {
        return tenantService.addAttributeDefinitions(tenantName, attributeDefinitions);
    }

    @ApiOperation(value = "Updates the given attribute definitions of the specified tenant")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_UPDATE_ATTRIBUTE_DEFINITIONS, method = RequestMethod.POST)
    @ResponseBody
    public Tenant updateAttributeDefinitions(@ApiParam("The tenant's name")
                                             @PathVariable(PATH_VAR_NAME) String tenantName,
                                             @ApiParam("The definitions to update (should have the same name as " +
                                                 "definitions that the tenant already has)")
                                             @RequestBody Collection<AttributeDefinition> attributeDefinitions)
            throws ProfileException {
        return tenantService.updateAttributeDefinitions(tenantName, attributeDefinitions);
    }

    @ApiOperation(value = "Removes the given attribute definitions from the specified tenant")
    @ApiImplicitParam(name = "accessTokenId", required = true, dataType = "string", paramType = "query",
                      value = "The ID of the application access token")
    @RequestMapping(value = URL_TENANT_REMOVE_ATTRIBUTE_DEFINITIONS, method = RequestMethod.POST)
    @ResponseBody
    public Tenant removeAttributeDefinitions(@ApiParam("The tenant's name")
                                             @PathVariable(PATH_VAR_NAME) String tenantName,
                                             @ApiParam("The name of the attributes whose definitions should be removed")
                                             @RequestParam(PARAM_ATTRIBUTE_NAME) Collection<String> attributeNames)
            throws ProfileException {
        return tenantService.removeAttributeDefinitions(tenantName, attributeNames);
    }

}
