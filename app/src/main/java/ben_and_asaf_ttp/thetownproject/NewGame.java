package ben_and_asaf_ttp.thetownproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ben_and_asaf_ttp.thetownproject.DB.DBHandler;

public class NewGame extends AppCompatActivity {
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        dbHandler = new DBHandler(this);
    }

    public void addBook(View v)
    {
        // get the 3 values from the screen
        String bookName  = gameDescription.getText().toString();
        String authorName = .getText().toString();
        int year = Integer.parseInt(yearEdit.getText().toString());

        Book b = new Book(bookName, authorName, year);

        if(dbHandler.addBook(b))
            Toast.makeText(this, "Book added succesfuly", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Book NOT added", Toast.LENGTH_SHORT).show();

    }

    public void viewBooks(View v)
    {
        ArrayList<Book> booksList = dbHandler.getAllBooks();

        // each round in the loop is a record in the DB
        for(Book b: booksList)  {

            Log.d("TESTDB", b.toString());
        }
    }
}
