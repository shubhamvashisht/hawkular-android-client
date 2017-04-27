/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.client.android.backend.model;

import java.util.Map;

import org.jboss.aerogear.android.core.RecordId;

import com.squareup.moshi.Json;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

public class Trigger {
    @RecordId
    @Json(name = "id")
    private String id;
    @Json(name = "tags")
    private Map<String, String> tags;
    @Json(name = "description")
    private String description;
    @Json(name = "enabled")
    private boolean enabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public boolean getEnableStatus(){
        return enabled;
    }

    public void setEnabledStatus(boolean enabled) {
        this.enabled=enabled;
    }

    @VisibleForTesting
    public Trigger(@NonNull String id, @NonNull Map<String, String> tags, @NonNull String description) {
        this.id = id;
        this.tags = tags;
        this.description = description;
    }

}
