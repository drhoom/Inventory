package com.example.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;
import com.example.android.inventory.data.InventoryDbHelper;

public class InventoryCursorAdapter extends CursorAdapter {
    private InventoryDbHelper dbHelper;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        dbHelper = new InventoryDbHelper(context);
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        Holder holder = new Holder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final Holder holder = (Holder) view.getTag();

        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);


        String productId = cursor.getString(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);

        holder.idTextView.setText(productId);
        holder.nameTextView.setText(productName);
        holder.priceTextView.setText(productPrice);
        holder.quantityTextView.setText(productQuantity);

        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int id = Integer.parseInt(holder.idTextView.getText().toString());
                int quantity = Integer.parseInt(holder.quantityTextView.getText().toString());
                int newQuantity = dbHelper.reduceQuantityByOne(id, quantity);
                if (newQuantity > -1) {
                    holder.quantityTextView.setText(Integer.toString(newQuantity));
                }
            }
        });
    }

    static private class Holder {
        TextView idTextView;
        TextView nameTextView;
        TextView quantityTextView;
        TextView priceTextView;
        Button saleButton;

        public Holder(View view) {
            idTextView = view.findViewById(R.id.productId);
            nameTextView = view.findViewById(R.id.name);
            priceTextView = view.findViewById(R.id.price);
            quantityTextView = view.findViewById(R.id.quantity);
            saleButton = view.findViewById(R.id.saleBtn);
        }
    }

}
