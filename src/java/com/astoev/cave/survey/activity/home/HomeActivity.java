package com.astoev.cave.survey.activity.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.main.BTActivity;
import com.astoev.cave.survey.activity.main.MainActivity;
import com.astoev.cave.survey.activity.poc.SensorTestActivity;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Project;

import java.util.List;

/**
 * 
 * @author astoev
 * @author jmitrev
 */
public class HomeActivity extends MainMenuActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
    }
    
    @Override
    protected void onResume() {
    	// first we reset the ws

    	// then we call the parent that will set a title depending on the active project
        super.onResume();
    	
        loadProjects();
    }
    
	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
	 */
	@Override
	protected int getChildsOptionsMenu() {
		return R.menu.homemenu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(Constants.LOG_TAG_UI, "Home menu selected - " + item.toString());
		switch (item.getItemId()) {
		case R.id.action_new_project:{
			newProjectOnClick();
			return true;
		}
		case R.id.action_setup_bt : {
			pairBtDevice();
			return true;
		}
		case R.id.action_azimuth_test : {
			onAzimuthTest();
			return true;
		}
        case R.id.main_action_about:
            showAboutDialog();
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    private void showAboutDialog() {
        try {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.about);
            TextView url = (TextView) dialog.findViewById(R.id.aboutUrl);
            Linkify.addLinks(url, Linkify.WEB_URLS);
            TextView url2 = (TextView) dialog.findViewById(R.id.aboutUrl2);
            Linkify.addLinks(url2, Linkify.WEB_URLS);
            dialog.show();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed toshow about", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void loadProjects() {
        try {

            ListView projectsContainer = (ListView) findViewById(R.id.homeProjects);
            
            final List<Project> projectsList = getWorkspace().getDBHelper().getProjectDao().queryForAll();
            
            if (projectsList.size() > 0) {
                Project[] projectsArray = new Project[projectsList.size()];
                projectsArray = projectsList.toArray(projectsArray);
            	
                // populate the projects in the list using adapter
            	ArrayAdapter<Project> projectsAdapter = new ArrayAdapter<Project>(this, android.R.layout.simple_list_item_1, projectsArray);
            	projectsContainer.setAdapter(projectsAdapter);
            	
            	// item clicked listener
            	OnItemClickListener projectClickedListener = new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

						Project project = (Project)parent.getAdapter().getItem(position);
						
                        Log.i(Constants.LOG_TAG_UI, "Selected project " + project.getId());
                        getWorkspace().setActiveProject(project);
                        Leg lastProjectLeg = getWorkspace().getLastLeg();
                        getWorkspace().setActiveLeg(lastProjectLeg);

                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(intent);
					}
				};
				
            	projectsContainer.setOnItemClickListener(projectClickedListener);
            	
            } else {
            	// no projects - show "No projects" label
            	String[] value = {getResources().getString(R.string.home_no_projects)};
            	ArrayAdapter<String> noprojectsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, value);
            	projectsContainer.setAdapter(noprojectsAdapter);

                projectsContainer.setAdapter(noprojectsAdapter);

                // item clicked listener
                OnItemClickListener projectClickedListener = new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Intent intent = new Intent(HomeActivity.this, NewProjectActivity.class);
                        startActivity(intent);
                    }
                };

                projectsContainer.setOnItemClickListener(projectClickedListener);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed offer project", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    /**
     * Action method that handles click on "Bluetooth device"  button
     */
    private void pairBtDevice() {
        Intent intent = new Intent(this, BTActivity.class);
        startActivity(intent);
    }

    /**
     * Action method that handles click on Add new project button
     */
    private void newProjectOnClick() {
        Log.i(Constants.LOG_TAG_UI, "New project");
        Intent intent = new Intent(this, NewProjectActivity.class);
        startActivity(intent);
    }
    
    private void onAzimuthTest(){
    	Log.i(Constants.LOG_TAG_UI, "Azimuth Test");
    	Intent intent = new Intent(this, SensorTestActivity.class);
    	startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.menu_exit_confirmation_question)
                .setCancelable(false)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(Constants.LOG_TAG_UI, "Exit app");
                        getWorkspace().clean();
                        HomeActivity.this.moveTaskToBack(true);
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }
}
