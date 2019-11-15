package com.example.armodule;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Config.CloudAnchorMode;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.codelab.cloudanchor.helpers.CloudAnchorManager;
import com.google.ar.core.codelab.cloudanchor.helpers.FirebaseManager;
import com.google.ar.core.codelab.cloudanchor.helpers.LibraryData;
import com.google.ar.core.codelab.cloudanchor.helpers.ResolveDialogFragment;
import com.google.ar.core.codelab.cloudanchor.helpers.SnackbarHelper;
import com.google.ar.core.codelab.cloudanchor.helpers.StorageManager;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.armodule.BarcodeScanner.SCAN_DATA;


public class ARview extends AppCompatActivity {




        private static final String TAG = ARview.class.getSimpleName();
        private static final double MIN_OPENGL_VERSION = 3.0;
        ArrayList<Anchor> anchorList = new ArrayList<Anchor>();

    private Scene arScene;
        private ArFragment arFragment;
        private ModelRenderable andyRenderable;
        private AnchorNode currentAnchorNode;
        private TextView tvDistance;
        private Anchor currentAnchor = null;
        private final StorageManager storageManager = new StorageManager();
        private Button resolveButton;
        private FirebaseManager firebaseManager;
        ArrayList<AnchorNode> anchorNodeList = new ArrayList<>();
        private static final String KEY_PREFIX = "anchor;";
        public static final String SHELF_VIEW = "shelf_view";

//        private final CloudAnchorManager cloudAnchorManager = new CloudAnchorManager();
//        private final SnackbarHelper snackbarHelper = new SnackbarHelper();

//    protected Config getSessionConfiguration(Session session) {
//        Config config = new Config(session);
//        config.setCloudAnchorMode(CloudAnchorMode.ENABLED);
//        return config;
//    }
        //private TextView tvData;
        String bookId;
    HashMap<String, ArrayList<String>> Books = new HashMap<>();

    private final CloudAnchorManager cloudAnchorManager = new CloudAnchorManager();
    private final SnackbarHelper snackbarHelper = new SnackbarHelper();
    private DatabaseReference rootRef;
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


            bookId = getIntent().getStringExtra(SCAN_DATA);
            resolveButton = findViewById(R.id.resolve_button);


        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);
        rootRef = FirebaseDatabase.getInstance(firebaseApp).getReference().child("shared_anchor_codelab_root");
        DatabaseReference.goOnline();

        // Read Local Database



        String line = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("data.csv")));

            while ((line = br.readLine()) != null) {
                String [] data = line.split(",");

                ArrayList<String> a = new ArrayList<>();
                a.add(data[0]);
                a.add(data[2]);
                Books.put(data[1],a);
            }

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"GET OUT 1",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"GET OUT 2 ",Toast.LENGTH_LONG).show();
         }


        //Toast.makeText(getApplicationContext(), bookId, Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_arview);
            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        Session session;
        try {
            session = new Session(this);
            Config c = new Config(session);
            c.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            c.setCloudAnchorMode(CloudAnchorMode.ENABLED);
            session.configure(c);

            /*DO FROM HERE ----------------------------------*/
            arFragment.getArSceneView().setupSession(session);


        } catch (UnavailableArcoreNotInstalledException e) {
            e.printStackTrace();
        } catch (UnavailableApkTooOldException e) {
            e.printStackTrace();
        } catch (UnavailableSdkTooOldException e) {
            e.printStackTrace();
        } catch (UnavailableDeviceNotCompatibleException e) {
            e.printStackTrace();
        }





            // When you build a Renderable, Sceneform loads its resources in the background while returning
            // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ModelRenderable.builder()
                        .setSource(this, R.raw.arrow)
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
        firebaseManager = new FirebaseManager(this);
            arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
                arFragment.onUpdate(frameTime);
                onUpdate();
            });

        arScene = arFragment.getArSceneView().getScene();
        //resolveButton.setEnabled(false);

        arScene.addOnUpdateListener(frameTime -> cloudAnchorManager.onUpdate());

            arFragment.setOnTapArPlaneListener(
                    (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                        if (andyRenderable == null) {
                            return;
                        }

                        //Log.d("BUZZZZ TESTINNGGG:", "TAPPEEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD OK NAAA???");

                        // Create the Anchor.
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        /*arScene = arFragment.getArSceneView().getScene();
                        //resolveButton.setEnabled(false);

                        arScene.addOnUpdateListener(frameTime -> cloudAnchorManager.onUpdate());
                        */
                        anchorNode.setParent(arScene);
                        anchorList.add(anchor);
                        anchorNodeList.add(anchorNode);
                        //clearAnchor();

                        currentAnchor = anchor;
                        currentAnchorNode = anchorNode;

                        snackbarHelper.showMessage(arFragment.getActivity(), "Now hosting anchor...");
                        cloudAnchorManager.hostCloudAnchor(
                                arFragment.getArSceneView().getSession(), currentAnchor, this::onHostedAnchorAvailable);

                        // Create the transformable andy and add it to the anchor.
                        TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                        andy.setParent(anchorNode);
                        andy.setRenderable(andyRenderable);
                        andy.select();
                    });


        }


        public void onUpdate() {
            Frame frame = arFragment.getArSceneView().getArFrame();
            tvDistance = findViewById(R.id.tvDistance);
            Log.d("API123", "onUpdateframe... current anchor node " + (currentAnchorNode == null));

            Log.d("BUZZZZZZ TEsSTINGGG::: ","00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
            if (currentAnchorNode != null && currentAnchor != null) {
                Pose objectPose = currentAnchor.getPose();
                Pose cameraPose = frame.getCamera().getPose();

                float dx = objectPose.tx() - cameraPose.tx();
                float dy = objectPose.ty() - cameraPose.ty();
                float dz = objectPose.tz() - cameraPose.tz();

                ///Compute the straight-line distance.
                float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                tvDistance.setText("Distance from camera: " + distanceMeters + " metres");
                //Log.d("BUZZZZZZ TEsSTINGGG::: ","Distance from camera:"  + Float.toString(distanceMeters) +  "metres");
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

    /*public void distanceCalculator(View view){
        Pose startPose = anchorList.get(0).getPose();
        Pose endPose = anchorList.get(1).getPose();

// Clean up the anchor
      *//*session.removeAnchors(Collections.singleton(startAnchor));
      startAnchor = null;*//*

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
    }*/

    private synchronized void setNewAnchor(@Nullable Anchor anchor) {
        /*if (currentAnchorNode != null) {
            // If an AnchorNode existed before, remove and nullify it.
            arScene.removeChild(currentAnchorNode);
            currentAnchorNode = null;
        }*/
        if (anchor != null) {
            if (andyRenderable == null) {
                // Display an error message if the renderable model was not available.
                Toast toast = Toast.makeText(getApplicationContext(), "Andy model was not loaded.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            // Create the Anchor.
            currentAnchorNode = new AnchorNode(anchor);
            arScene.addChild(currentAnchorNode);

            // Create the transformable andy and add it to the anchor.
            TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
            andy.setParent(currentAnchorNode);
            andy.setRenderable(andyRenderable);
            andy.select();
        }
    }

    public synchronized void onClearButtonPressed(View view) {
        // Clear the anchor from the scene.

        // The next line is the new addition.
        cloudAnchorManager.clearListeners();
        resolveButton.setEnabled(true);
        setNewAnchor(null);
    }

    private synchronized void onHostedAnchorAvailable(Anchor anchor) {
        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            String cloudAnchorId = anchor.getCloudAnchorId();
            firebaseManager.nextShortCode(shortCode -> {
                if (shortCode != null) {

                LibraryData ld = new LibraryData(bookId,cloudAnchorId);
               //firebaseManager.storeUsingShortCode(shortCode, cloudAnchorId);
               rootRef.child(KEY_PREFIX+shortCode).setValue(ld);
                    snackbarHelper
                            .showMessage(arFragment.getActivity(), "Cloud Anchor Hosted. Short code: " + shortCode);
                } else {
                    // Firebase could not provide a short code.
                    snackbarHelper
                            .showMessage(arFragment.getActivity(), "Cloud Anchor Hosted, but could not "
                                    + "get a short code from Firebase.");
                }
            });
            setNewAnchor(anchor);
        } else {
            snackbarHelper.showMessage(arFragment.getActivity(), "Error while hosting: " + cloudState.toString());
        }
    }

    public synchronized void onResolveButtonPressed(View view) {
        ResolveDialogFragment dialog = ResolveDialogFragment.createWithOkListener(
                this::onShortCodeEntered);;
        dialog.show(arFragment.getFragmentManager(), "Resolve");
    }

    private synchronized void onShortCodeEntered(int shortCode) {
        firebaseManager.getCloudAnchorId(shortCode, cloudAnchorId -> {
            if (cloudAnchorId == null || cloudAnchorId.isEmpty()) {
                snackbarHelper.showMessage(
                        arFragment.getActivity(),
                        "A Cloud Anchor ID for the short code " + shortCode + " was not found.");
                return;
            }


            //Toast.makeText(this,cloudAnchorId,Toast.LENGTH_LONG).show();
            //resolveButton.setEnabled(false);
            cloudAnchorManager.resolveCloudAnchor(
                    arFragment.getArSceneView().getSession(),
                    cloudAnchorId,
                    anchor -> onResolvedAnchorAvailable(anchor, shortCode));
        });
    }

    private synchronized void onResolvedAnchorAvailable(Anchor anchor, int shortCode) {
        Toast.makeText(this,"CALLED",Toast.LENGTH_LONG).show();
        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            Toast.makeText(this,"ITS WORKING",Toast.LENGTH_LONG).show();
            snackbarHelper.showMessage(arFragment.getActivity(), "Cloud Anchor Resolved. Short code: " + shortCode);
            setNewAnchor(anchor);
        } else {
            Toast.makeText(this,"ITS NOT WORKING",Toast.LENGTH_LONG).show();
            snackbarHelper.showMessage(
                    arFragment.getActivity(),
                    "Error while resolving anchor with short code "
                            + shortCode
                            + ". Error: "
                            + cloudState.toString());
            resolveButton.setEnabled(true);
        }
    }
    public void onResolveAllButtonPressed(View view){
        //DO HERE
        //tvData = findViewById(R.id.textView);


        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();


                        if(key.startsWith("anchor;")){
                            String bId = snapshot.child("BookId").getValue(String.class);
                            //tvData.append(bId+"\t");
                            int shortCode = Integer.parseInt(key.substring(key.length()-3));
                            //Toast.makeText(getApplicationContext(),String.valueOf(bId.equals(bookId)),Toast.LENGTH_LONG).show();
                            if(bId.equals(bookId)){
                                firebaseManager.getCloudAnchorId(shortCode,cloudAnchorId -> {
                                    if (cloudAnchorId == null || cloudAnchorId.isEmpty()) {
                                        snackbarHelper.showMessage(
                                                arFragment.getActivity(),
                                                "A Cloud Anchor ID for the short code " + shortCode + " was not found.");
                                        return;
                                    }
                                    //Toast.makeText(getApplicationContext(),cloudAnchorId,Toast.LENGTH_LONG).show();
                                    //resolveButton.setEnabled(false);

                                    cloudAnchorManager.resolveCloudAnchor(
                                            arFragment.getArSceneView().getSession(),
                                            cloudAnchorId,
                                            anchor -> onResolvedAnchorAvailable(anchor, shortCode));
                                });
                            }

                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"BYE", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onShelfButtonPressed(View view){
        Intent intent = new Intent(this, ShelfDisplay.class);
        intent.putExtra(SHELF_VIEW,Books.get(bookId).get(1));
        startActivity(intent);
    }
}


