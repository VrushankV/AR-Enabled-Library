package com.example.armodule;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;

public class ARview extends AppCompatActivity {




        private static final String TAG = ARview.class.getSimpleName();
        private static final double MIN_OPENGL_VERSION = 3.0;
        ArrayList<Anchor> anchorList = new ArrayList<Anchor>();


        private ArFragment arFragment;
        private ModelRenderable andyRenderable;
        private AnchorNode currentAnchorNode;
        private TextView tvDistance;
        private Anchor currentAnchor = null;


        @Override
        @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
        // CompletableFuture requires api level 24
        // FutureReturnValueIgnored is not valid
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (!checkIsSupportedDeviceOrFinish(this)) {
                Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
                return;
            }

            setContentView(R.layout.activity_arview);
            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

            tvDistance = findViewById(R.id.tvDistance);

            // When you build a Renderable, Sceneform loads its resources in the background while returning
            // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ModelRenderable.builder()
                        .setSource(this, R.raw.andy)
                        .build()
                        .thenAccept(renderable -> andyRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast toast =
                                            Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    return null;
                                });
            }

            arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
                arFragment.onUpdate(frameTime);
                onUpdate();
            });

            arFragment.setOnTapArPlaneListener(
                    (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                        if (andyRenderable == null) {
                            return;
                        }

                        Log.d("BUZZZZ TESTINNGGG:", "TAPPEEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD OK NAAA???");

                        // Create the Anchor.
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());
                        anchorList.add(anchor);
                        clearAnchor();

                        currentAnchor = anchor;
                        currentAnchorNode = anchorNode;

                        // Create the transformable andy and add it to the anchor.
                        TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                        andy.setParent(anchorNode);
                        andy.setRenderable(andyRenderable);
                        andy.select();
                    });
        }

        public void onUpdate() {
            Frame frame = arFragment.getArSceneView().getArFrame();

            Log.d("API123", "onUpdateframe... current anchor node " + (currentAnchorNode == null));

            Log.d("BUZZZZZZ TEsSTINGGG::: ","00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
            if (currentAnchorNode != null) {
                Pose objectPose = currentAnchor.getPose();
                Pose cameraPose = frame.getCamera().getPose();

                float dx = objectPose.tx() - cameraPose.tx();
                float dy = objectPose.ty() - cameraPose.ty();
                float dz = objectPose.tz() - cameraPose.tz();

                ///Compute the straight-line distance.
                float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                tvDistance.setText("Distance from camera: " + distanceMeters + " metres");
                Log.d("BUZZZZZZ TEsSTINGGG::: ","Distance from camera:"  + Float.toString(distanceMeters) +  "metres");
                /*float[] distance_vector = currentAnchor.getPose().inverse()
                        .compose(cameraPose).getTranslation();
                float totalDistanceSquared = 0;
                for (int i = 0; i < 3; ++i)
                    totalDistanceSquared += distance_vector[i] * distance_vector[i];*/
            }
        }

        /**
         * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
         * on this device.
         *
         * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
         *
         * <p>Finishes the activity if Sceneform can not run
         */
        public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                Log.e(TAG, "Sceneform requires Android N or later");
                Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
                activity.finish();
                return false;
            }
            String openGlVersionString =
                    ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                            .getDeviceConfigurationInfo()
                            .getGlEsVersion();
            if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
                Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                        .show();
                activity.finish();
                return false;
            }
            return true;
        }

    public void distanceCalculator(View view){
        Pose startPose = anchorList.get(0).getPose();
        Pose endPose = anchorList.get(1).getPose();

// Clean up the anchor
      /*session.removeAnchors(Collections.singleton(startAnchor));
      startAnchor = null;*/

// Compute the difference vector between the two hit locations.
        float dx = startPose.tx() - endPose.tx();
        float dy = startPose.ty() - endPose.ty();
        float dz = startPose.tz() - endPose.tz();

// Compute the straight-line distance.
        float distanceMeters = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

        Toast.makeText(getApplicationContext(),"Hello"+ distanceMeters,Toast.LENGTH_LONG).show();
    }

    private void clearAnchor() {
        currentAnchor = null;


        if (currentAnchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(currentAnchorNode);
            currentAnchorNode.getAnchor().detach();
            currentAnchorNode.setParent(null);
            currentAnchorNode = null;
        }
    }
}


