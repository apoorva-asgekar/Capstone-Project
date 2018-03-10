package com.example.android.kidventures.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.example.android.kidventures.R;
import com.example.android.kidventures.ui.MainActivity;
import com.example.android.kidventures.ui.SearchResultsActivity;

/**
 * Implementation of App Widget functionality.
 */
public class KidventuresWidgetProvider extends AppWidgetProvider {

  private static int RC_ZOO = 1;
  private static int RC_MUSEUM = 2;
  private static int RC_RESTAURANT = 3;
  private static int RC_BEACH = 4;

  static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId) {

    CharSequence widgetText = context.getString(R.string.appwidget_text);
    // Construct the RemoteViews object
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.kidventures_widget);
    views.setTextViewText(R.id.appwidget_text, widgetText);

    //Setting pendingIntents for each of the ImageViews
    views.setOnClickPendingIntent(R.id.zoo,
            getPendingIntent(context,  RC_ZOO, context.getString(R.string.widget_zoo_query)));
    views.setOnClickPendingIntent(R.id.museum,
            getPendingIntent(context, RC_MUSEUM, context.getString(R.string.widget_museum_query)));
    views.setOnClickPendingIntent(R.id.restaurant,
            getPendingIntent(context, RC_RESTAURANT, context.getString(R.string.widget_restaurant_query)));
    views.setOnClickPendingIntent(R.id.beach,
            getPendingIntent(context, RC_BEACH, context.getString(R.string.widget_beach_query)));

    //Setting pendingIntent for the search icon
    Intent mainActivityIntent = new Intent(context, MainActivity.class);
    PendingIntent pendingIntent =
            PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
    views.setOnClickPendingIntent(R.id.search, pendingIntent);

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views);
  }

  private static PendingIntent getPendingIntent(Context context, int requestCode, String query) {
    Intent searchActivityIntent = new Intent(context, SearchResultsActivity.class);
    Bundle queryBundle = new Bundle();
    queryBundle.putString(MainActivity.SEARCH_QUERY, query);
    searchActivityIntent.putExtras(queryBundle);
    return PendingIntent.getActivity(context, requestCode, searchActivityIntent, 0);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // There may be multiple widgets active, so update all of them
    for (int appWidgetId : appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId);
    }
  }

  @Override
  public void onEnabled(Context context) {
    // Enter relevant functionality for when the first widget is created
  }

  @Override
  public void onDisabled(Context context) {
    // Enter relevant functionality for when the last widget is disabled
  }
}

