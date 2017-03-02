package com.bradly;

import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.internal.LatLngAdapter;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Scanner;


public class Main {

    //Use this scanner to read text data that will be stored in String variables
    static Scanner stringScanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception{

        String geoKey = null;
        String userRequest = null;
        String userChoice="0";
        boolean foundPlace = false;
        boolean foundChoice = false;
        boolean newLookup = true;

        //look up access key
        try (BufferedReader reader = new BufferedReader(new FileReader("geokey.txt"))){
            geoKey = reader.readLine();
        }
        catch (IOException ex){
            System.out.println(ex);
        }
        //allow for user to restart the search
        while(newLookup) {
            foundPlace = false;
            //ask user for location
            System.out.println("Where would you like to look up?");
            userRequest = stringScanner.nextLine();

            GeoApiContext codingcontext = new GeoApiContext().setApiKey(geoKey);

            GeocodingResult[] codingresults = GeocodingApi.newRequest(codingcontext).address(userRequest).await();
            if (codingresults.length > 0) {
                foundPlace = true;
            }
            //check to see if the location was found, if not then keep asking until one is
            while (foundPlace == false) {

                System.out.println("Where would you like to look up?");
                userRequest = stringScanner.nextLine();

                codingresults = GeocodingApi.newRequest(codingcontext).address(userRequest).await();

                if (codingresults.length > 0) {
                    foundPlace = true;
                } else {
                    System.out.println("Location not found!");
                }
            }
            //if multiple results, list them then ask user for selection
            if (codingresults.length > 1) {
                while (foundChoice == false) {
                    for (int x = 0; x < codingresults.length; x++) {
                        System.out.println(x + " " + codingresults[x].formattedAddress);
                    }
                    System.out.println("Which location do you want? Type new for a new lookup.");
                    userChoice = stringScanner.nextLine();
                    if(userChoice.equalsIgnoreCase("new")){
                        break;
                    }
                    else if (Integer.parseInt(userChoice) < codingresults.length) {
                        foundChoice = true;
                    } else {
                        System.out.println(userChoice + " is not a valid choice!");
                    }
                }
            }
            else if (codingresults.length==1){
                foundChoice = true;
            }
            if(foundChoice) {
                //get the lat and long values
                LatLng resLatLng = codingresults[Integer.parseInt(userChoice)].geometry.location;

                ElevationResult[] results = ElevationApi.getByPoints(codingcontext, resLatLng).await();

                ElevationResult resElevation = results[0];

                //display the election for the selected location
                System.out.println(String.format("The elevation of " + codingresults[Integer.parseInt(userChoice)].formattedAddress + " is %.2f meters.", resElevation.elevation));

                newLookup = false;
            }
        }
    }
}
