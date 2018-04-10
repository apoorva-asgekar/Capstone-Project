This repository has the functional document and the code for the Capstone Project required to complete the Android Developer Nanodegree.

About the App
Ever struggled to decide where to take the kids for a couple of hours or even to spend the entire day? Ever wondered if the fancy restaurant your best friend has suggested for a dinner with family has food options for your little one? This app answers all those questions and provides you with more resources to ensure your family outings are adventurous for all the right reasons.
This app lets parents: 
- Search for kid-friendly activities locally- personalize the search as per the age of their kids, categories like museums etc.
- Look for kid-friendly food options.
- Check out kids menus, bathroom/changing table options, stroller renting options, kids activities
- Review an exciting place they recently visited with their kids to help out other parents looking for similar options.
- Give a kidventurous rating to this place.


Please generate and add the API keys for the following APIs -
1) Google Places Web API - add the key to the following location:
   File - com.example.android.kidventures.utilities.NetworkUtils.java at line number 30.
   Line - private final static String GOOGLE_PLACES_API_KEY = "";
2) Android Places/Maps API - add the key to the followin location:
   File - app/src/main/AndroidManifest.xml at line number 53.
   Line - android:name="com.google.android.geo.API_KEY" android:value=""
If the API keys above are not added then the application will not be able to fetch the data.
