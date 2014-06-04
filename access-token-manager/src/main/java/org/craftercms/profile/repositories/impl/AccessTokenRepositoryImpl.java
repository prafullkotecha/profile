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
package org.craftercms.profile.repositories.impl;

import org.craftercms.commons.mongo.AbstractJongoRepository;
import org.craftercms.commons.mongo.MongoDataException;
import org.craftercms.profile.api.AccessToken;
import org.craftercms.profile.repositories.AccessTokenRepository;

/**
 * Default implementation of {@link org.craftercms.profile.repositories.AccessTokenRepository}, using Jongo.
 *
 * @author avasquez
 */
public class AccessTokenRepositoryImpl extends AbstractJongoRepository<AccessToken> implements AccessTokenRepository {

    public static final String FIND_BY_ID_QUERY_KEY =   "accessToken.byId";
    public static final String REMOVE_BY_ID_QUERY_KEY = "accessToken.removeById";

    @Override
    public AccessToken findById(String id) throws MongoDataException {
        return findOne(getQueryFor(FIND_BY_ID_QUERY_KEY), id);
    }

    @Override
    public void removeById(String id) throws MongoDataException {
        remove(getQueryFor(REMOVE_BY_ID_QUERY_KEY), id);
    }

}
