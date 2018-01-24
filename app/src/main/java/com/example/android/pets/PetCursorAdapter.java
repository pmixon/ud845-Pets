package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.R;
import com.example.android.pets.data.PetContract;

/**
 * Created by pmixon on 1/23/18.
 */

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.pet_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);

        String name = cursor.getString(nameColumnIndex);
        String breed = cursor.getString(breedColumnIndex);

        TextView nameTextView = (TextView) view.findViewById(R.id.pet_name);
        TextView breedTextView = (TextView) view.findViewById(R.id.pet_breed);

        nameTextView.setText(name);
        breedTextView.setText(breed);
    }

}
