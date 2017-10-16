package com.example.android.inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.provider.MediaStore.Images.Media;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_INVENTORY_LOADER = 0;
    private Uri mCurrentInventoryUri;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private Button mIncQuantity;
    private Button mDecQuantity;
    private Button mContact;
    private Button mSelectImage;
    private ImageView mProductImageView;
    private byte mProductImage[];
    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();


        if (mCurrentInventoryUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));

            invalidateOptionsMenu();
        } else {

            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mIncQuantity = (Button) findViewById(R.id.incQuantity);
        mDecQuantity = (Button) findViewById(R.id.decQuantity);
        mContact = (Button) findViewById(R.id.contact);
        mSelectImage = (Button) findViewById(R.id.select_image);
        mProductImageView = (ImageView) findViewById(R.id.product_image);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);

        mIncQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProductHasChanged = true;
                String quantityText = mQuantityEditText.getText().toString();
                if (TextUtils.isEmpty(quantityText))
                    mQuantityEditText.setText("1");
                else {
                    int quantity = Integer.parseInt(quantityText);
                    mQuantityEditText.setText(Integer.toString(++quantity));
                }
            }
        });

        mDecQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProductHasChanged = true;
                String quantityText = mQuantityEditText.getText().toString();
                if (TextUtils.isEmpty(quantityText) || quantityText.equals("0"))
                    mQuantityEditText.setText("0");
                else {
                    int quantity = Integer.parseInt(quantityText);
                    mQuantityEditText.setText(Integer.toString(--quantity));
                }
            }
        });

        mContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "supplier@example.com");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Wanted extra quantity");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Please provide us with and extra quantity of the product " + mNameEditText.getText().toString());
                startActivity(Intent.createChooser(emailIntent, ""));
            }
        });

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmapImage = Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, baoStream);
                mProductImage = baoStream.toByteArray();
                mProductImageView.setImageBitmap(bitmapImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean saveProduct() {

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentInventoryUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Please make sure to enter all information",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, mProductImage);

        if (mCurrentInventoryUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentInventoryUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveProduct())
                    finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_IMAGE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY};

        return new CursorLoader(this,
                mCurrentInventoryUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            byte productImage[] = cursor.getBlob(imageColumnIndex);
            if (productImage != null) {
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(productImage, 0, productImage.length);
                mProductImageView.setImageBitmap(bitmapImage);
            } else {
                mProductImageView.setImageResource(R.mipmap.ic_launcher);
            }
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mProductImageView.setImageResource(R.mipmap.ic_launcher);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentInventoryUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}