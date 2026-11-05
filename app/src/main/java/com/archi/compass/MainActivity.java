package com.archi.compass;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.view.View;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;
import android.graphics.Typeface;

public class MainActivity extends Activity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor accelerometer, magnetometer;

    float[] gravity = new float[3];
    float[] magnetic = new float[3];
    float[] rotation = new float[9];
    float[] orientation = new float[3];

    TextView headingText, directionText, statusText;
    CompassView compassView;

    float azimuth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(5, 12, 28));
        getWindow().setNavigationBarColor(Color.rgb(5, 12, 28));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(25, 30, 25, 30);
        root.setBackgroundColor(Color.rgb(5, 12, 28));

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S");
        title.setTextColor(Color.rgb(0, 220, 255));
        title.setTextSize(30);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("ADVANCED DIGITAL COMPASS");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);

        compassView = new CompassView(this);

        headingText = new TextView(this);
        headingText.setText("0°");
        headingText.setTextColor(Color.WHITE);
        headingText.setTextSize(46);
        headingText.setGravity(Gravity.CENTER);

        directionText = new TextView(this);
        directionText.setText("NORTH");
        directionText.setTextColor(Color.rgb(0, 220, 255));
        directionText.setTextSize(22);
        directionText.setGravity(Gravity.CENTER);

        statusText = new TextView(this);
        statusText.setText("SYSTEM INITIALIZING...");
        statusText.setTextColor(Color.GREEN);
        statusText.setTextSize(13);
        statusText.setGravity(Gravity.CENTER);

        root.addView(title);
        root.addView(subtitle);

        root.addView(compassView, new LinearLayout.LayoutParams(
                -1, 0, 1));

        root.addView(headingText);
        root.addView(directionText);
        root.addView(statusText);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);

        if (magnetometer != null)
            sensorManager.registerListener(this, magnetometer,
                    SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values.clone();

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magnetic = event.values.clone();

        if (SensorManager.getRotationMatrix(rotation, null, gravity, magnetic)) {

            SensorManager.getOrientation(rotation, orientation);

            azimuth = (float) Math.toDegrees(orientation[0]);
            azimuth = (azimuth + 360) % 360;

            int degrees = Math.round(azimuth) % 360;

            String[] directions = {
                    "NORTH", "NORTH-EAST", "EAST", "SOUTH-EAST",
                    "SOUTH", "SOUTH-WEST", "WEST", "NORTH-WEST"
            };

            int index = Math.round(degrees / 45f) % 8;

            headingText.setText(degrees + "°");
            directionText.setText(directions[index]);
            statusText.setText("● COMPASS SENSOR ONLINE");
            compassView.invalidate();
        } else {
            statusText.setText("CALIBRATE SENSOR");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            statusText.setText("CALIBRATE: MOVE PHONE IN FIGURE 8");
    }

    class CompassView extends View {

        Paint paint = new Paint(3);

        CompassView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = Math.min(cx, cy) * 0.78f;

            paint.setColor(Color.rgb(0, 210, 255));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);

            canvas.drawCircle(cx, cy, radius, paint);
            canvas.drawCircle(cx, cy, radius * 0.85f, paint);

            canvas.save();
            canvas.rotate(-azimuth, cx, cy);

            for (int i = 0; i < 360; i += 15) {
                float r1 = radius * 0.88f;
                float r2 = i % 45 == 0 ? radius * 0.72f : radius * 0.80f;

                paint.setStrokeWidth(i % 45 == 0 ? 3 : 1);

                double angle = Math.toRadians(i - 90);

                canvas.drawLine(
                        cx + (float)Math.cos(angle) * r1,
                        cy + (float)Math.sin(angle) * r1,
                        cx + (float)Math.cos(angle) * r2,
                        cy + (float)Math.sin(angle) * r2,
                        paint
                );
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(radius * 0.15f);
            paint.setTypeface(Typeface.DEFAULT_BOLD);

            String[] labels = {"N", "E", "S", "W"};

            for (int i = 0; i < 4; i++) {
                double angle = Math.toRadians(i * 90 - 90);

                float x = cx + (float)Math.cos(angle) * radius * 0.62f;
                float y = cy + (float)Math.sin(angle) * radius * 0.62f;

                paint.setColor(i == 0 ? Color.RED : Color.WHITE);
                canvas.drawText(labels[i], x - 10, y + 10, paint);
            }

            canvas.restore();

            paint.setColor(Color.rgb(0, 220, 255));
            paint.setStyle(Paint.Style.FILL);

            canvas.drawCircle(cx, cy, 8, paint);
        }
    }
}
