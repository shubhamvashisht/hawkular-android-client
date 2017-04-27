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

import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.android.core.RecordId;

import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.Json;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

public final class Alert {
    @RecordId
    @Json(name = "id")
    private String id;

    @Json(name = "severity")
    private String severity;

    @Json(name = "status")
    private String status;

    @Json(name = "ctime")
    private long timestamp;

    @Json(name = "evalSets")
    private List<ArrayList<AlertEvaluation>> evaluations;

    @Json(name = "notes")
    private List<Note> notes;

    @Json(name = "trigger")
    private Trigger trigger;

    @VisibleForTesting
    public Alert(@NonNull String id, long timestamp, @NonNull List<ArrayList<AlertEvaluation>> evaluations, @NonNull String severity,
                 @NonNull String status, @NonNull List<Note> notes) {
        this.id = id;
        this.timestamp = timestamp;
        this.evaluations = evaluations;
        this.severity = severity;
        this.status = status;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSeverity() {
        return severity;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<ArrayList<AlertEvaluation>> getEvaluations() {
        return evaluations;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public List<Note> getNotes() {
        return notes;
    }

}

