/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.imageclassification;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import org.tensorflow.lite.examples.imageclassification.databinding.ActivityMainBinding;

/** Entrypoint for app */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private NavController navController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        setSupportActionBar(activityMainBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // Setup BottomNavigationView with NavController
            NavigationUI.setupWithNavController(activityMainBinding.bottomNavigation, navController);

            // Configure segments that shouldn't show a back button
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.camera_fragment, R.id.history_fragment, R.id.settings_fragment
            ).build();
            
            // Connect Action Bar with NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Hide/Show Toolbar and BottomNav based on fragment
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.permissions_fragment) {
                    activityMainBinding.toolbar.setVisibility(View.GONE);
                    activityMainBinding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    activityMainBinding.toolbar.setVisibility(View.VISIBLE);
                    activityMainBinding.bottomNavigation.setVisibility(View.VISIBLE);
                    if (activityMainBinding.toolbarTitle != null) {
                        activityMainBinding.toolbarTitle.setText(destination.getLabel());
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition();
        } else {
            super.onBackPressed();
        }
    }
}
