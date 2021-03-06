package nz.ac.cornell.fitnessmealplans.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import nz.ac.cornell.fitnessmealplans.Models.Category;
import nz.ac.cornell.fitnessmealplans.Models.Menu;

/**
 * This Dao class helps to control the database
 * By using this Dao, system can access to category, menu, personalMenu table
 *
 * @author Jiyoon An
 * @date 03-06-16
 */
public class DaoCategory {

    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DaoCategory instance;


    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DaoCategory(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DaoCategory getInstance(Context context) {
        if (instance == null) {
            instance = new DaoCategory(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        try {
            this.database = openHelper.getWritableDatabase();
        } catch (SQLException e) {
            close();
        }
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }


    /**
     * Read all categories from the category table.
     *
     * @return an ArrayList of category
     */
    public ArrayList<Category> getCategory() {
        ArrayList<Category> categoryList = new ArrayList<Category>();
        ArrayList<Menu> menuList = getMenu();
        ArrayList<Menu> tempList;
        Cursor cursor = database.rawQuery("SELECT * FROM category", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            tempList = new ArrayList<Menu>();
            for(int i=0; i<menuList.size();i++) {
                if((cursor.getString(0)).equals(menuList.get(i).getCategoryId())) {
                    tempList.add(menuList.get(i));
                }
            }
            categoryList.add(new Category(cursor.getString(0), cursor.getString(1), tempList));
            cursor.moveToNext();
        }
        return categoryList;
    }

    /**
     * load all data from category table with out menulist
     * @return
     */
    public ArrayList<Category> getSimpleCategory() {
        ArrayList<Category> list = new ArrayList<Category>();
        Cursor cursor = database.rawQuery("SELECT * FROM category", null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            list.add(new Category(cursor.getString(0), cursor.getString(1), null));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * load all data from menu table
     * @return
     */
    public ArrayList<Menu> getMenu() {
        ArrayList<Menu> list = new ArrayList<Menu>();
        Cursor cursor = database.rawQuery("SELECT * FROM menu", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(new Menu(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getDouble(3), cursor.getString(4), cursor.getString(5), ""));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * load all data from personalMenu table with userId
     * @param userID
     * @return
     */
    public ArrayList<Menu> getPersonalMenu(String userID) {
        ArrayList<Menu> list = new ArrayList<Menu>();
        Cursor cursor = database.rawQuery("SELECT * FROM personalMenu WHERE userID = '" + userID + "'", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(new Menu(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getDouble(3), NulltoString(cursor.getString(4)), NulltoString(cursor.getString(5)), cursor.getString(6)));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * load last id from personalMenu table
     * @return
     */
    public String getPersonalMenuId() {
        String menuID = null;
        Cursor cursor = database.rawQuery("SELECT menuID FROM personalMenu WHERE menuID = (SELECT MAX(menuID) FROM personalMenu)", null);
        cursor.moveToFirst();
        if (cursor.getCount()!=0) {
            menuID = cursor.getString(0);
        }
        cursor.close();
        return menuID;
    }

    /**
     * load all data from personalMenu with menuId
     * @param menuId
     * @return
     */
    public Menu getChosenPersonalMenu(String menuId) {
        Menu menu = null;
        Cursor cursor = database.rawQuery("SELECT * FROM personalMenu WHERE menuID = '" + menuId + "'", null);
        cursor.moveToFirst();
        if(cursor.getCount()!=0) {
            menu = new Menu(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getDouble(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
        }
        cursor.close();
        return menu;
    }

    /**
     * insert data to personalMenu table
     * @param menuId
     * @param menuName
     * @param amount
     * @param calories
     * @param codeId
     * @param categoryId
     * @param userId
     */
    public void setPersonalMenu(String menuId, String menuName, String amount, double calories, String codeId, String categoryId, String userId) {

        database = openHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("menuID", menuId);
        values.put("menuName", menuName);
        values.put("amount", amount);
        values.put("calories", calories);
        values.put("codeID", codeId);
        values.put("categoryID", categoryId);
        values.put("userID", userId);

        database.insert("personalMenu", null, values);
    }

    /**
     * update all data to personalMenu table
     * @param menuId
     * @param menuName
     * @param amount
     * @param calories
     * @param codeId
     * @param categoryId
     * @param userId
     */
    public void updatePersonalMenu(String menuId, String menuName, String amount, double calories, String codeId, String categoryId, String userId) {

        database = openHelper.getWritableDatabase();

        try {
            String query = String.format("UPDATE personalMenu " +
                    " SET menuName = '" + menuName +"', amount = '" + amount + "', calories = '" + calories + "', codeID = '"
                    + codeId + "', categoryID = '" + categoryId + "' WHERE menuID = '" + menuId + "';");
            database.execSQL(query);

        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.d("Exception", e.getMessage());
        }
    }

    /**
     * load the name of menu by using the menu ID from the menu and personalMenu
     * @param menuId
     * @return
     */
    public Menu getMenuName(String menuId) {
        Cursor cursor = database.rawQuery("SELECT menuName, calories FROM menu WHERE menuId='" + menuId + "'", null);
        if(cursor.getCount()<=0) {
            cursor = database.rawQuery("SELECT menuName, calories FROM personalMenu WHERE menuId='" + menuId + "'", null);
        }
        cursor.moveToFirst();
        Menu menu = null;
        if(cursor.getCount()>0) {
            menu = new Menu(cursor.getString(0), cursor.getDouble(1));
        }

        cursor.close();
        return menu;
    }

    public ArrayList<Menu>  getRecipeList(String codeId){
        ArrayList<Menu> list = new ArrayList<Menu>();

        String query = "SELECT * FROM menu WHERE CodeID like '%" + codeId + "%'";
        Log.d("SELECT Menu Recipe : ", query);
        // database.execSQL(query);

        // Cursor cursor = database.rawQuery("SELECT * FROM menu WHERE CodeID like '%" + codeId + "%'", null);
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(new Menu(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getDouble(3), cursor.getString(4), cursor.getString(5), ""));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    /**
     * delete the data from personalMenu
     * @param menuId
     */
    public void setDeletePersonalMenu(String menuId) {
        database.delete("personalMenu", "menuID = ?", new String[] {menuId});
        database.close();
    }

    /**
     * load the category id from menu table by using menuId
     * @param menuId
     * @return
     */
    public String getMenuCategory(String menuId) {
        Cursor cursor = database.rawQuery("SELECT categoryID FROM menu WHERE menuID = '" + menuId + "'", null);
        if(cursor.getCount()<=0){
            cursor = database.rawQuery("SELECT categoryID FROM personalMenu WHERE menuID = '" + menuId + "'", null);
        }
        cursor.moveToFirst();
        String menuCategory = cursor.getString(0);
        cursor.close();
        return menuCategory;
    }

    /**
     * load only calories data from menu and personalMenu table by using menuId
     * @param menuId
     * @return
     */
    public double getCalory(String menuId) {
        Cursor cursor = database.rawQuery("SELECT calories FROM menu WHERE menuID = '" +menuId + "'", null);
        if(cursor.getCount()<=0) {
            cursor = database.rawQuery("SELECT calories FROM personalMenu WHERE menuID = '"+ menuId + "'", null);
        }
        cursor.moveToFirst();
        double calories = cursor.getDouble(0);
        cursor.close();
        return calories;
    }

    private String NulltoString(String data) {
        if(data == null) {
            data = "";
        }
        return data;
    }

}
