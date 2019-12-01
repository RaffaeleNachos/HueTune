# HueTune
HueTune is an application for Android that advices music based on input photos and pictures. The main idea is about an user that take a picture of what is he doing or where he is, the app processes that picture with *TensorFlow* and a *Spotify* song is returned related to that picture content.

## How it works
At the *onCreate()* of the main Activity a temporary token is asked to *Spotify* via API call (Using *Volley* asynchronously). The user has the possibility to take a picture from Camera or pick images from Gallery. After the picture is returned to the main Activity it's URI/PATH is stored in the database with other informations such as the location and the picture rotation. Then an AsyncTask starts to process the image with [*TensorFlow Lite Quantized Model*](https://www.tensorflow.org/lite/models/image_classification/overview). TFLite elaborates the bitmap and understands what's in the image. Then an API call to *Spotify* is sent with the informations that TFLite found. The result of the *Spotify* API call is the best fitting song for the informations given.

## Some Techniques
The main View is a ListView populated by a CursorAdapter that takes informations from the database. The pictures are loaded by an AsyncTask that process the Bitmap, optionally rotates it and cuts it in square shape. (For university purpose, I've made my own way to load images asynchronously, but in my code is included a second loading method that uses [*Glide*](https://bumptech.github.io/glide/) library **much more efficient than mine, it also uses caching**). 
There's a global menu in the toolbar that gives you the option to go to the Bin Activity where the pictures deleted are stored, they also stay there for 30 days then are deleted (during this 30 days the user have the possibility to restore them). 
Permissions (GPS, CAMERA, STORAGE and INTERNET) are requested at run-time using DialogCompats.
ListView items menus are managed by Context menus that pop-up with long press on single ListView entries. There you can set the Location of the photo by using the actual current position, it is requested to the Google Play Services using Coarse Location.

## Screenshots
<img src="https://imgur.com/kJe0BDX.jpg" width="220"> <img src="https://imgur.com/0k7vj1d.jpg" width="220"> <img src="https://imgur.com/0j3msKE.jpg" width="220">
