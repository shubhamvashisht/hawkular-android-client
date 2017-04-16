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
package org.hawkular.client.android.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hawkular.client.android.R;
import org.hawkular.client.android.activity.AlertDetailActivity;
import org.hawkular.client.android.adapter.AlertsAdapter;
import org.hawkular.client.android.backend.BackendClient;
import org.hawkular.client.android.backend.model.Alert;
import org.hawkular.client.android.backend.model.Resource;
import org.hawkular.client.android.backend.model.Trigger;
import org.hawkular.client.android.util.ColorSchemer;
import org.hawkular.client.android.util.ErrorUtil;
import org.hawkular.client.android.util.Fragments;
import org.hawkular.client.android.util.Intents;
import org.hawkular.client.android.util.Time;
import org.hawkular.client.android.util.ViewDirector;
import org.jboss.aerogear.android.pipe.callback.AbstractSupportFragmentCallback;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import timber.log.Timber;

/**
 * Alerts fragment.
 * <p/>
 * Displays alerts as a list with menus allowing some alert-related actions, such as acknowledgement and resolving.
 */

public class AlertsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        AlertsAdapter.AlertListener {

    @BindView(R.id.list) RecyclerView recyclerView;
    @BindView(R.id.content) SwipeRefreshLayout swipeRefreshLayout;

    public ArrayList<Trigger> triggers;
    public ArrayList<Alert> alerts;
    public ArrayList<Alert> alertsDump;
    public boolean isActionPlus;
    public int alertsTimeMenu;
    public boolean isAlertsFragmentAvailable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        isAlertsFragmentAvailable = true;
        setUpState(state);
        setUpBindings();
        setUpList();
        setUpMenu();
        isActionPlus = false;
        setUpRefreshing();
        setUpAlertsUi();
    }

    private void setUpState(Bundle state) {
        Icepick.restoreInstanceState(this, state);
    }

    private void setUpBindings() {
        ButterKnife.bind(this, getView());
    }

    private void setUpList() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setUpMenu() {
        setHasOptionsMenu(true);
    }

    private void setUpRefreshing() {
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(ColorSchemer.getScheme());
    }

    @OnClick(R.id.button_retry)
    public void setUpAlertsUi() {
        if (alerts == null) {
            alertsTimeMenu = R.id.menu_time_hour;

            setUpAlertsForced();
        } else {
            setUpAlerts(alertsDump);
        }
    }

    private void setUpAlertsRefreshed() {
        setUpAlerts();
    }

    private void setUpAlertsForced() {
        showProgress();
        setUpAlerts();
    }

    private void setUpAlerts() {
        if(getResource() == null) {
            BackendClient.of(this).getAlerts(getAlertsTime(), Time.current(), null, new AlertsCallback());
        } else if (!areTriggersAvailable()) {
            setUpTriggers();
        } else {
            BackendClient.of(this).getAlerts(getAlertsTime(), Time.current(), triggers, new AlertsCallback());
        }
    }

    private boolean areTriggersAvailable() {
        return (triggers != null) && !triggers.isEmpty();
    }

    private void setUpTriggers() {
        BackendClient.of(this).getTriggers(new TriggersCallback());
    }

    private Date getAlertsTime() {
        switch (alertsTimeMenu) {
            case R.id.menu_time_hour:
                return Time.hourAgo();

            case R.id.menu_time_day:
                return Time.dayAgo();

            case R.id.menu_time_week:
                return Time.weekAgo();

            case R.id.menu_time_month:
                return Time.monthAgo();

            case R.id.menu_time_year:
                return Time.yearAgo();

            default:
                return Time.hourAgo();
        }
    }

    private void showProgress() {
        ViewDirector.of(this).using(R.id.animator).show(R.id.progress);
    }

    private void setUpAlertsTriggers(List<Trigger> triggers) {
        this.triggers = new ArrayList<>(filterTriggers(triggers));

        setUpAlerts();
    }

    private List<Trigger> filterTriggers(List<Trigger> triggers) {
        // TODO: think about better backend API.
        // This is mostly a hack, as trigger usage at all, actually.
        // Caused by a lack of API connecting Inventory and Alerts components.

        Resource resource = getResource();
        List<Trigger> filteredTriggers = new ArrayList<>();

        for (Trigger trigger : triggers) {
            if (trigger.getTags() != null
                    && trigger.getTags().get("resourceId").equals(resource.getId())) {
                filteredTriggers.add(trigger);
            }
        }

        return filteredTriggers;
    }

    private Resource getResource() {
        return getArguments().getParcelable(Fragments.Arguments.RESOURCE);
    }

    private void setUpAlerts(final List<Alert> alerts) {
        this.alertsDump = new ArrayList<>(alerts);

        sortAlerts(this.alertsDump);

        if (isActionPlus) {
            this.alerts = alertsDump;
            if (this.alerts != null) {
                recyclerView.setAdapter(new AlertsAdapter(getActivity(), this, this.alerts));
            }
        } else {
            this.alerts = removeResolved();
            if (this.alerts != null) {
                recyclerView.setAdapter(new AlertsAdapter(getActivity(), this, this.alerts));
            }
        }

        hideRefreshing();

        if(this.alerts.isEmpty()) {
            showMessage();
        } else {
            showList();
        }
    }

    private ArrayList<Alert> removeResolved() {
        this.alerts = new ArrayList<>();
        for (Alert alert : alertsDump) {
            if (!alert.getStatus().equals("RESOLVED"))
                alerts.add(alert);
        }
        return alerts;
    }

    private void sortAlerts(List<Alert> alerts) {
        Collections.sort(alerts, new AlertsComparator());
    }

    @Override
    public void onAlertBodyClick(View alertView, int alertPosition) {
        Intent intent = new Intent(getActivity(), AlertDetailActivity.class);
        Alert alert = getAlertsAdapter().getItem(alertPosition);
        intent.putExtra(Intents.Extras.ALERT, alert);
        startActivity(intent);
    }

    @Override
    public void onAlertMenuClick(View alertView, int alertPosition) {
        showAlertMenu(alertView, alertPosition);
    }

    private void showAlertMenu(final View alertView, final int alertPosition) {
        PopupMenu alertMenu = new PopupMenu(getActivity(), alertView);

        alertMenu.getMenuInflater().inflate(R.menu.popup_alerts, alertMenu.getMenu());

        alertMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Alert alert = getAlertsAdapter().getItem(alertPosition);

                switch (menuItem.getItemId()) {
                    case R.id.menu_resolve:
                        BackendClient.of(AlertsFragment.this).resolveAlert(alert, new AlertActionCallback());
                        return true;

                    case R.id.menu_acknowledge:
                        BackendClient.of(AlertsFragment.this).acknowledgeAlert(alert, new AlertActionCallback());
                        return true;

                    default:
                        return false;
                }
            }
        });

        alertMenu.show();
    }

    private AlertsAdapter getAlertsAdapter() {
        return (AlertsAdapter) recyclerView.getAdapter();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.toolbar_alerts, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(alertsTimeMenu).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_time_hour:
            case R.id.menu_time_day:
            case R.id.menu_time_week:
            case R.id.menu_time_month:
            case R.id.menu_time_year:
                alertsTimeMenu = menuItem.getItemId();
                menuItem.setChecked(true);

                setUpAlertsForced();

                return true;

            case R.id.show_hide_res:
                isActionPlus = !isActionPlus;
                setUpAlerts(alertsDump);
                if(isActionPlus){
                    menuItem.setTitle(R.string.hide_resolved);
                }
                else{
                    menuItem.setTitle(R.string.show_resolved);
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void cleanDump() {
        this.alertsDump = new ArrayList<>();
    }

    private void hideRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showList() {
        ViewDirector.of(this).using(R.id.animator).show(R.id.content);
    }

    private void showMessage() {
        ViewDirector.of(this).using(R.id.animator).show(R.id.message);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        tearDownState(state);
    }

    private void tearDownState(Bundle state) {
        Icepick.saveInstanceState(this, state);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        isAlertsFragmentAvailable = false;

        tearDownBindings();
    }

    private void tearDownBindings() {
        //TODO: Modify it
        //ButterKnife.unbind(this);
    }

    @Override
    public void onRefresh() {

    }

    private static final class TriggersCallback extends AbstractSupportFragmentCallback<List<Trigger>> {
        @Override
        public void onSuccess(List<Trigger> triggers) {
            if (triggers.isEmpty()) {
                Timber.d("Triggers list is empty, this should not happen.");

                ErrorUtil.showError(getAlertsFragment(),R.id.animator,R.id.error);
                return;
            }

            getAlertsFragment().setUpAlertsTriggers(triggers);
        }

        @Override
        public void onFailure(Exception e) {
            Timber.d(e, "Triggers fetching failed.");

            ErrorUtil.showError(getAlertsFragment(),R.id.animator,R.id.error);
        }

        private AlertsFragment getAlertsFragment() {
            return (AlertsFragment) getSupportFragment();
        }
    }

    private static final class AlertsCallback extends AbstractSupportFragmentCallback<List<Alert>> {
        @Override
        public void onSuccess(List<Alert> alerts) {
            if (getAlertsFragment().isAlertsFragmentAvailable) {
                if (!alerts.isEmpty()) {
                    getAlertsFragment().setUpAlerts(alerts);
                } else {
                    getAlertsFragment().showMessage();
                    getAlertsFragment().cleanDump();
                }
            }
        }

        @Override
        public void onFailure(Exception e) {
            Timber.d(e, "Alerts fetching failed.");

            if (getAlertsFragment().isAlertsFragmentAvailable) {
                ErrorUtil.showError(getAlertsFragment(),R.id.animator,R.id.error);
            }
        }

        private AlertsFragment getAlertsFragment() {
            return (AlertsFragment) getSupportFragment();
        }
    }

    private static final class AlertActionCallback extends AbstractSupportFragmentCallback<List<String>> {
        @Override
        public void onSuccess(List<String> result) {
            getAlertsFragment().setUpAlertsRefreshed();
        }

        @Override
        public void onFailure(Exception e) {
        }

        private AlertsFragment getAlertsFragment() {
            return (AlertsFragment) getSupportFragment();
        }
    }

    private static final class AlertsComparator implements Comparator<Alert> {
        @Override
        public int compare(Alert leftAlert, Alert rightAlert) {
            Date leftAlertTimestamp = new Date(leftAlert.getTimestamp());
            Date rightAlertTimestamp = new Date(rightAlert.getTimestamp());

            return leftAlertTimestamp.compareTo(rightAlertTimestamp);
        }
    }
}
