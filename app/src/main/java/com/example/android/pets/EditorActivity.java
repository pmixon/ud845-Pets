/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

import org.w3c.dom.Text;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CREATED_COLUMN_INTENT = "com.example.android.pets.CREATED_COLUMN_NUMBER";

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;
    private Uri mCurrentPetUri;

    private boolean mPetHasChanged = false;

    static final int PET_EDIT_LOADER = 1;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);


        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        Intent intent = this.getIntent();
        mCurrentPetUri = intent.getData();

        if (mCurrentPetUri == null) {
            setTitle(getString(R.string.new_pet_title));
            invalidateOptionsMenu();

        } else {
            Toast.makeText(this, mCurrentPetUri.toString(), Toast.LENGTH_LONG).show();
            setTitle(getString(R.string.edit_pet_title));
            getLoaderManager().initLoader(PET_EDIT_LOADER,null,this);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private void savePet() {

        String petName = mNameEditText.getText().toString();
        String breed = mBreedEditText.getText().toString();
        String selectedSex = mGenderSpinner.getSelectedItem().toString();
        String weightString = mWeightEditText.getText().toString().trim();

        if (TextUtils.isEmpty(petName) && TextUtils.isEmpty(breed) && TextUtils.isEmpty(weightString)) {
            return;
        }

        if (selectedSex == getString(R.string.gender_male)) {
            mGender = 1;
        }
        else if (selectedSex == getString(R.string.gender_female)) {
            mGender = 2;
        }


        int weight;
        if (weightString == null || TextUtils.isEmpty(weightString)) {
            weight = 0;
        } else {
            weight = Integer.parseInt(weightString);
        }

        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, petName);
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, weight);

        Uri createdColumnUri;
        int rowsAffected;
        if (mCurrentPetUri != null) {
            rowsAffected = getContentResolver().update(mCurrentPetUri,values,null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.pet_update_fail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.pet_update_success, Toast.LENGTH_SHORT).show();
            }
        } else {
            createdColumnUri = getContentResolver().insert(PetEntry.CONTENT_URI,values);

            if (createdColumnUri == null) {
                Toast.makeText(this, R.string.error_saving_pet_message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.pet_saved_message, Toast.LENGTH_SHORT).show();
            }
        }



    }

    public void deletePet() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_record);
        builder.setPositiveButton(R.string.confirm_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int column;
                column = getContentResolver().delete(mCurrentPetUri, null, null);
                if (dialog != null) {
                    dialog.dismiss();
                    finish();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                finish();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:

                deletePet();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void finish() {
        Intent intent = new Intent(EditorActivity.this, CatalogActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED,
                PetContract.PetEntry.COLUMN_PET_GENDER,
                PetContract.PetEntry.COLUMN_PET_WEIGHT
        };

        return new CursorLoader(this, mCurrentPetUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int petNameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int petBreedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int petWeightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
            int petSexColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);

            String petName = cursor.getString(petNameColumnIndex);
            String petBreed = cursor.getString(petBreedColumnIndex);
            String petWeight = cursor.getString(petWeightColumnIndex);
            int petSex = cursor.getInt(petSexColumnIndex);

            mNameEditText.setText(petName);
            mBreedEditText.setText(petBreed);
            mWeightEditText.setText(petWeight);

            switch (petSex) {
                case PetEntry.GENDER_UNKNOWN:
                    mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
                    break;
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(PetEntry.GENDER_MALE);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(PetEntry.GENDER_FEMALE);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        clearInputFields();
    }

    public void clearInputFields() {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(0);
        mWeightEditText.setText("");
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_message);
        builder.setPositiveButton(R.string.discard_changes_messages, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing_message, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {

        if (mPetHasChanged == false) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(onClickListener);
    }
}