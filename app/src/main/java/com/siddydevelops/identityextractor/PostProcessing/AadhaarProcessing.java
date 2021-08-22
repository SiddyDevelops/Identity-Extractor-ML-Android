package com.siddydevelops.identityextractor.PostProcessing;

import android.content.Context;
import android.graphics.Rect;
import android.widget.Toast;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class AadhaarProcessing {

    public HashMap<String, String> processExtractTextForFrontPic(FirebaseVisionText text, Context context)
    {
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        if(blocks.size() == 0)
        {
            Toast.makeText(context, "No Text Found!", Toast.LENGTH_SHORT).show();
            return null;
        }
        TreeMap<String, String> map = new TreeMap<>();

        for(FirebaseVisionText.Block block : text.getBlocks())
        {
            for(FirebaseVisionText.Line line : block.getLines())
            {
                Rect rect = line.getBoundingBox();
                String y = String.valueOf(rect.exactCenterY());
                String lineText = line.getText().toLowerCase();
                map.put(y, lineText);
            }
        }

        List<String> orderData = new ArrayList<>(map.values());
        HashMap<String, String> dataMap = new HashMap<>();

        int i = 0;
        String regx = "\\d\\d\\d\\d([,\\s])?\\d\\d\\d\\d.*";

        for(i =0; i<orderData.size(); i++)
        {
            if(orderData.get(i).matches(regx))
            {
                dataMap.put("aadhaar",orderData.get(i));
                break;
            }
        }

        //Setting gender first
        for(i=0; i<orderData.size(); i++)
        {
            if(orderData.get(i).contains("female"))
            {
                dataMap.put("gender","Female");
                break;
            }
            else if(orderData.get(i).contains("male"))
            {
                dataMap.put("gender","Male");
                break;
            }
        }

        if(!dataMap.containsKey("aadhaar"))
        {
            if(i+1 < orderData.size())
            {
                dataMap.put("aadhaar",orderData.get(i+1));
            }
        }

        //Searching for Father's Name
        for(i=0;i<orderData.size(); i++)
        {
            if(orderData.get(i).contains("father"))
            {
                dataMap.put("fatherName",orderData.get(i).replace("father","").replace(":",""));
                break;
            }
        }

        if(dataMap.containsKey("fatherName"))
        {
            if(i-2 > -1)
            {
                dataMap.put("name",orderData.get(i-2));
            }
        }

        //Search for year of birth
        for(i =0; i< orderData.size(); i++)
        {
            if(orderData.get(i).contains("dob"))
            {
                dataMap.put("yob",orderData.get(i).substring(orderData.get(i).length()-10));
                break;
            }
        }

        if(i-1 > -1 && !orderData.get(i-1).contains("father"))
        {
            dataMap.put("name", orderData.get(i-1));
        }

        return dataMap;

    }

    public HashMap<String, String> processExtractedForBackPic(FirebaseVisionText text, Context context)
    {
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        if(blocks.size() == 0)
        {
            Toast.makeText(context, "No Text Found!", Toast.LENGTH_SHORT).show();
            return null;
        }
        TreeMap<String, String> map = new TreeMap<>();

        for(FirebaseVisionText.Block block : text.getBlocks())
        {
            for(FirebaseVisionText.Line line : block.getLines())
            {
                Rect rect = line.getBoundingBox();
                String y = String.valueOf(rect.exactCenterY());
                String lineText = line.getText().toLowerCase();
                map.put(y, lineText);
            }
        }

        List<String> orderData = new ArrayList<>(map.values());
        HashMap<String, String> dataMap = new HashMap<>();

        int i = 0;

        //Address First
        for(i=0;i<orderData.size(); i++)
        {
            if(orderData.get(i).contains("address"))
            {
                dataMap.put("addressLine1",orderData.get(i).replace("address","").replace(":",""));
                break;
            }
        }

        if(i+1 < orderData.size())
        {
            dataMap.put("addressLine2",orderData.get(i+1));
        }

        if(i+2 < orderData.size())
        {
            dataMap.put("addressLine3",orderData.get(i+2));
        }

        if(i+3 < orderData.size())
        {
            dataMap.put("addressLine4",orderData.get(i+3));
        }
        else {
            dataMap.put("addressLine4","Nothing");
        }

        //Pincode
        String regex = "\\d\\d\\d\\d\\d\\d";

        for(i=0; i< orderData.size(); i++)
        {
            if(orderData.get(i).matches(regex) || orderData.contains(".*" + regex))
            {
                dataMap.put("pincode",orderData.get(i));
            }
        }

        return dataMap;

    }

}
