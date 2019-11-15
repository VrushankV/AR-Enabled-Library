package com.example.armodule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class ShelfView extends View {


    private Rect rectangle;
    private Paint paint;
    public ShelfView(Context context) {
        super(context);
        int x = 50;
        int y = 50;
        int sideLength = 200;

        //int x_cord = x+(Integer.parseInt(BookPos.substring(1,1))-1)*540;
        //int y_cord = y+(Integer.parseInt(BookPos.substring(1,1))-1)*420;

        // create a rectangle that we'll draw later
        //rectangle = new Rect(x_cord,y_cord , x_cord+sideLength, y_cord+sideLength);
        rectangle = new Rect(x,y,sideLength,sideLength);
        // create the Paint and set its color
        paint = new Paint();
        paint.setColor(Color.YELLOW);
    }

    public ShelfView(Context context,String BookPos) {
        super(context);
        int x = 50;
        int y = 50;
        int sideLength = 200;

        int x_cord = x+(Character.getNumericValue(BookPos.charAt(1))-1)*540;
        int y_cord = y+(Character.getNumericValue(BookPos.charAt(2))-1)*420;


        // create a rectangle that we'll draw later
        rectangle = new Rect(x_cord,y_cord , x_cord+sideLength, y_cord+sideLength);
        //rectangle = new Rect(x,y,sideLength,sideLength);
        // create the Paint and set its color
        paint = new Paint();
        paint.setColor(Color.YELLOW);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Point screenPts = new Point();

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.shelf);
        canvas.drawBitmap(bmp,screenPts.x,screenPts.y , null);
        canvas.drawRect(rectangle, paint);
    }
}
