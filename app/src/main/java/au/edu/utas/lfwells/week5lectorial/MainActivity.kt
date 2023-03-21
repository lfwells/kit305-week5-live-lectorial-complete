package au.edu.utas.lfwells.week5lectorial

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity()
{
    //request permission contract for getting access to camera
    //in: string permission
    //out: boolean permission granted
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionResult ->
        if (permissionResult)
        {
            takeAPicture()
        }
        else {}
    }

    //contract for taking a picture
    //in: file URI
    //out: boolean picture taken
    private val getCameraResult = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { cameraResult ->
        if (cameraResult) //did we successfully take a photo?
        {
            setPic(findViewById<ImageView>(R.id.myImg))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //make the button request permission and take a picture
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        btnCamera.setOnClickListener {
            requestTakeAPicture()
        }


        //here is the database code -- uncomment to play

        //root ui object, going to use for displaying image views
        val root = findViewById<ConstraintLayout>(R.id.root)

        //get the image utils object which can draw image views for us
        val imageUtils = ImageUtils()

        //get db connection
        val db = Firebase.firestore
        Log.d("FIREBASE", "Firebase connected: ${Firebase.app}")

        //just drawing a pixel
        //imageUtils.drawPixelAsImageView(root, 0, 0, -5000, 100)

        val imageCollection = db.collection("my_image")

        //reading one document with a known id
        /*
        val docRef = imageCollection.document("0208H1qThxUOPIaQ8aNO") //generated id
        Log.d("FIREBASE", docRef.id)
        docRef.get().addOnSuccessListener { result ->
            val pixel = result.get("pixel") as Long
            Log.d("FIREBASE", "pixel: $pixel")
            imageUtils.drawPixelAsImageView(root, 0, 0, pixel.toInt(), 100)
        }.addOnFailureListener {
            //document not found
        }
        */

        //get all documents in the collection (with queries)
        /*
        Log.d("FIREBASE" , "1")
        //SELECT FROM my_image WHERE pixel < -500000

        imageCollection
            .whereLessThan("pixel", -5500000)
            //.whereEqualTo("x", 0)
            //.limit(10)
            .get()
            .addOnSuccessListener { result ->
                Log.d("FIREBASE", "A"+result.size().toString())
                for (document in result)
                {
                    val pixel = document.get("pixel") as Long
                    val x = document.get("x") as Long
                    val y = document.get("y") as Long
                    Log.d("FIREBASE", "pixel: $pixel")
                    imageUtils.drawPixelAsImageView(root, x.toInt(), y.toInt(), pixel.toInt(), 16)
                }
            }

        Log.d("FIREBASE" , "2")
        imageCollection.count().get(AggregateSource.SERVER).addOnSuccessListener { result ->
            Log.d("FIREBASE", "B"+result.count.toString())
        }

        Log.d("FIREBASE" , "3")
        */
    }

    //more camera functions (self-explanatory function names)
    private fun requestTakeAPicture()
    {
        //this will automatically go to takeAPicture() if permission previously granted
        requestPermission.launch(android.Manifest.permission.CAMERA)
    }

    //can only get to here if the permission has been granted
    private fun takeAPicture()
    {
        Log.d("CAMERA", "time to take a pic")

        val photoFile: File = createImageFile()!!
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "au.edu.utas.lfwells.week5lectorial",
            photoFile
        )
        getCameraResult.launch(photoURI)
    }

    //boilerplate from android docs
    //reserves a spot in the file system for a photo
    //out: the file path of the photo (via global variable currentPhotoPath)
    lateinit var currentPhotoPath: String
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    //boilerplate from android docs
    //https://developer.android.com/training/camera/photobasics
    //reads in an image and resizes it to match the dimensions of the image view
    //respects aspect ratio
    //in: the image view to place it in
    //in: the file path of the image (via global variable currentPhotoPath)
    private fun setPic(imageView: ImageView) {
        // Get the dimensions of the View
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            imageView.setImageBitmap(bitmap)
        }
    }

}