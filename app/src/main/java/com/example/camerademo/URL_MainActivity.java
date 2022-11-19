package com.example.camerademo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URL_MainActivity extends AppCompatActivity {

    Button btnAdd, btnPrev, btnNext, btnCamera;
    EditText inputURL;
    ImageView imageView;

    ArrayList<String> imageURLs = new ArrayList<>();

    private int currentIndex = 0;
    private static final String FILE_NAME = "URLs.txt";

    // Regex pattern for URL validation
    String regex = "https?://(?:[a-z0-9\\-]+\\.)+[a-z]{2,6}(?:/[^/#?]+)+\\.(?:png|jpg|gif|jpeg)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_main);

        // retrieve all ui elements on the form
        findAllElements();
        whenClickAdd();
        setImage();
        loadImage();
        whenClickNext();
        whenClickPrevious();
        whenClickCamera();
    }

    private void loadImage() {
        try {
            loadURLs();
            if (imageURLs.size() > 0 && imageURLs.size() != 1) {
                currentIndex = 0;
            } else {
                Glide.with(this).load(R.drawable.no_img).into(imageView);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Read file error, file = " + FILE_NAME, Toast.LENGTH_SHORT).show();
        }
    }

    private void whenClickCamera() {
        // Switch to Camera Activity
        btnCamera.setOnClickListener(view -> startActivity(new Intent(URL_MainActivity.this, Camera_MainActivity.class)));

    }

    private void whenClickAdd() {
        btnAdd.setOnClickListener(v -> {
            String URL = inputURL.getText().toString().trim();
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(URL);
            if (URL.isEmpty()) {
                inputURL.setError("Please enter a URL");
                inputURL.requestFocus();
            } else {
                if (m.matches()) {
                    imageURLs.add(URL);
                    try {
                        saveToFile(URL);
                        Toast.makeText(this, "URL added successfully", Toast.LENGTH_SHORT).show();
                        Glide.with(this).load(URL).into(imageView);
                        inputURL.setText("");
                        currentIndex = imageURLs.indexOf(URL);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Save File Error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    inputURL.setError("Please enter invalid URL");
                    inputURL.requestFocus();
                }
            }
        });
    }


    private void saveToFile(String url) throws IOException {
        FileOutputStream fileOutputStream = getApplicationContext().openFileOutput(FILE_NAME, Context.MODE_APPEND);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        bufferedWriter.write(url);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        bufferedWriter.close();
        outputStreamWriter.close();
    }

    private void whenClickPrevious() {
        btnPrev.setOnClickListener(v -> {
            currentIndex--;
            setImage();
        });
    }

    private void whenClickNext() {
        btnNext.setOnClickListener(v -> {
            currentIndex++;
            setImage();
        });
    }

    private void loadURLs() throws IOException {
        FileInputStream fileInputStream = getApplicationContext().openFileInput(FILE_NAME);
        if (fileInputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String lineData =
                    bufferedReader.readLine();
            while (lineData != null) {
                imageURLs.add(lineData);
                lineData = bufferedReader.readLine();
            }
        }
    }

    private void setImage() {
        int size = imageURLs.size();
        if (currentIndex >= size) {
            currentIndex = 0;
        } else if (currentIndex < 0) {
            currentIndex = size - 1;
        }
        if (size > 0) {
            Glide.with(this)
                    .load(imageURLs.get(currentIndex))
                    .into(imageView);
        }
    }

    private void findAllElements() {
        // findViewById
        btnAdd = findViewById(R.id.btnAdd);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnCamera = findViewById(R.id.btnCamera);
        inputURL = findViewById(R.id.inputURL);
        imageView = findViewById(R.id.imageCamera);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All?");
        builder.setMessage("Are you sure you want to delete all Images?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Delete all images
                if (item.getItemId() == R.id.delete_all) {
                    imageURLs.clear();
                    // Delete all images from file
                    imageView.setImageResource(0);
                    removeFile();
                    currentIndex = 0;
                    Toast.makeText(URL_MainActivity.this, "All Images deleted", Toast.LENGTH_SHORT).show();
                }
                //Refresh Activity
                Intent intent = new Intent(URL_MainActivity.this, URL_MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
        return super.onOptionsItemSelected(item);
    }

    private void removeFile() {
        getApplicationContext().deleteFile(FILE_NAME);
    }
}