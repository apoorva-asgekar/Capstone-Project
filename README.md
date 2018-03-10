This repository has the functional document and the code for the Capstone Project required to complete the Android Developer Nanodegree.

Please generate and add the API keys for the following APIs -
1) Google Places Web API - add the key to the following location:
   File - com.example.android.kidventures.utilities.NetworkUtils.java at line number 30.
   Line - private final static String GOOGLE_PLACES_API_KEY = "";
2) Android Places/Maps API - add the key to the followin location:
   File - app/src/main/AndroidManifest.xml at line number 53.
   Line - android:name="com.google.android.geo.API_KEY" android:value=""
If the API keys above are not added then the application will not be able to fetch the data.
