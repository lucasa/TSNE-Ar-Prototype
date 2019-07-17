package com.ar.tsne.ardemo

import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture


class CustomArFragment : ArFragment() {
    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
        return config
    }
}

class MainActivity : AppCompatActivity() {

    //private lateinit var anchorNode: AnchorNode
    private var trackingDisabled: Boolean = false
    private lateinit var modelRenderable: ModelRenderable
    private val TAG = "TSNE"

    private lateinit var arFragment: ArFragment

    private var isTracking: Boolean = false
    private var isHitting: Boolean = false

    private val words: MutableMap<Int, String> = mutableMapOf(1 to "hello", 2 to "world", 3 to "how", 4 to "are", 5 to "you")
    private val wordsRenderable: MutableMap<Int, Renderable> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)

        this.arFragment = sceneform_fragment as CustomArFragment

        // Adds a listener to the ARSceneView
        // Called before processing each frame
        this.arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            this.arFragment.onUpdate(frameTime)
            //if (!this.trackingDisabled)
            this.onUpdate()
        }

        // Set the onclick lister for our button
        // Change this string to point to the .sfb file of your choice :)
        floatingActionButton.setOnClickListener { addObjects() }
        this.showFab(false)

    }

    // Simple function to show/hide our FAB
    private fun showFab(enabled: Boolean) {
        if (enabled) {
            floatingActionButton.isEnabled = true
            floatingActionButton.visibility = View.VISIBLE
        } else {
            floatingActionButton.isEnabled = false
            floatingActionButton.visibility = View.GONE
        }
    }

    // Updates the tracking state
    private fun onUpdate() {
        updateTracking()
        // Check if the devices gaze is hitting a plane detected by ARCore
        if (isTracking) {
            //Log.d(TAG, "Is tracking")
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                Log.d(TAG, "Is hitting " + isHitting)
                showFab(isHitting)
            }
        }
    }

    // Performs frame.HitTest and returns if a hit is detected
    private fun updateHitTest(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                //Log.d(TAG, "Hit at " + hit.hitPose.translation.asList().toString())
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    // Makes use of ARCore's camera state and returns true if the tracking state has changed
    private fun updateTracking(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame?.camera?.trackingState == TrackingState.TRACKING
        return isTracking != wasTracking
    }

    // Simply returns the center of the screen
    private fun getScreenCenter(): Point {
        val view = findViewById<View>(android.R.id.content)
        return Point(view.width / 2, view.height / 2)
    }

    /**
     * @param model The Uri of our 3D sfb file
     *
     * This method takes in our 3D model and performs a hit test to determine where to place it
     */
    private fun addObjects() {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                Log.d(TAG, "Hit happened")
                val trackable = hit.trackable
                Log.d(TAG, "Trackable is " + trackable.javaClass.simpleName)
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    Log.d(TAG, "Adding 3d model asset...")
                    placeObject(arFragment, hit.createAnchor())
                    Log.d(TAG, "Added 3d model asset...")
                    break
                }
            }
        }
    }

    private fun createWordCloud(anchor: AnchorNode) {
        val sceneView = arFragment.arSceneView
        val pos = anchor.worldPosition
        Log.d(TAG, "create cloud at " + pos.toString())

        val lparams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        for (i in words.keys) {
            val word = words[i]
            val textView = TextView(this)
            textView.text = word
            textView.layoutParams = lparams
            textView.setTextAppearance(R.style.fontAR)

            ViewRenderable.builder()
                    .setView(this, textView)
                    .build()
                    .thenAccept({
                        wordsRenderable.put(i, it)
                        var dx = 0.1 * i
                        var dy = 0.5 + (0.1 * i)
                        var dz = 0.1 * i
                        Log.d(TAG, "created new text renderable " + i + " [" + word + "]")
                        Log.d(TAG, "dx" + i + " = " + dx)
                        Log.d(TAG, "dy" + i + " = " + dy)
                        Log.d(TAG, "dz" + i + " = " + dz)
                        this.addSomeText(it, sceneView, pos, dx, dy, dz)
                    })
        }
    }

    private fun addSomeText(textRenderable: Renderable, sceneView: ArSceneView, pos: Vector3, dx: Double, dy: Double, dz: Double) {
        val position = Vector3.add(pos, Vector3(dx.toFloat(), dy.toFloat(), dz.toFloat()))
        Log.d(TAG, "added text renderable to " + position)

        // Create an ARCore Anchor at the position.
        val pose = Pose.makeTranslation(position.x, position.y, position.z)
        val anchor = sceneView.session!!.createAnchor(pose)

        // Create the Sceneform AnchorNode
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(sceneView.getScene())

        // Create the node relative to the AnchorNode
        val node = Node()
        node.setParent(anchorNode)

        node.renderable = textRenderable

        //Renderable mycopy = textRenderable.makeCopy();
    }

    /**
     * @param fragment our fragment
     * @param anchor ARCore anchor from the hit test
     * @param model our 3D model of choice
     *
     * Uses the ARCore anchor from the hitTest result and builds the Sceneform nodes.
     * It starts the asynchronous loading of the 3D model using the ModelRenderable builder.
     */
    private fun placeObject(fragment: ArFragment, anchor: Anchor) {

        val renderable = this.createRenderables()

        renderable?.thenAccept {
            Log.d(TAG, "Placing the model at " + anchor.pose.toString())
            this.addNodeToScene(fragment, anchor, this.modelRenderable)

            //this.disablePlaneFinding()
        }!!
                .exceptionally {
                    Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
                    return@exceptionally null
                }
        /*ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept {
                    Log.d(TAG, "Placing the model at " + anchor.pose.toString())
                    addNodeToScene(fragment, anchor, it)
                }
                .exceptionally {
                    Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
                    return@exceptionally null
                }*/
    }

    /**
     * @param fragment our fragment
     * @param anchor ARCore anchor
     * @param renderable our model created as a Sceneform Renderable
     *
     * This method builds two nodes and attaches them to our scene
     * The Anchor nodes is positioned based on the pose of an ARCore Anchor. They stay positioned in the sample place relative to the real world.
     * The Transformable node is our Model
     * Once the nodes are connected we select the TransformableNode so it is available for interactions
     */
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)

        if (true) {
            val node = Node()
            node.setParent(anchorNode)
            node.renderable = renderable
            fragment.arSceneView.scene.addChild(anchorNode)

            // TransformableNode means the user to move, scale and rotate the model
//            val node = TransformableNode(fragment.transformationSystem)
//            node.renderable = renderable
//            node.setParent(anchorNode)
//            fragment.arSceneView.scene.addChild(anchorNode)
//            node.select()

            Log.d(TAG, "TransformableNode added in AR scene " + anchor.pose.toString())
        }

        this.createWordCloud(anchorNode)
    }

    private fun createRenderables(): CompletableFuture<Void>? {
        /* When you build a Renderable, Sceneform loads model and related resources
 * in the background while returning a CompletableFuture.
 * Call thenAccept(), handle(), or check isDone() before calling get().
 */
        return MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.RED))
                .thenAccept { material -> this.modelRenderable = ShapeFactory.makeSphere(0.05f, Vector3(0.0f, 0.06f, 0.0f), material) }
    }

    private fun disablePlaneFinding() {
        Log.d(TAG, "Disabling plane finding...")

        val config = this.arFragment.arSceneView?.session?.config
        config?.planeFindingMode = Config.PlaneFindingMode.DISABLED
        this.arFragment.arSceneView?.session?.configure(config)

        if (this.arFragment.arSceneView?.session?.config?.planeFindingMode === Config.PlaneFindingMode.DISABLED) {
            trackingDisabled = true
            Log.d(TAG, "Plane finding disabled!")
        }
    }

}
