package com.siddydevelops.identityextractor.PostProcessing;

import android.content.Context;
import android.graphics.Rect;
import android.widget.Toast;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class PanProcessing {

    public HashMap<String, String> processText(FirebaseVisionText text, Context context)
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
        String regex = "\\w\\w\\w\\w\\w\\d\\d\\d\\d.*";

        for(i =0; i<orderData.size(); i++)
        {
            if(orderData.get(i).contains("/"))
            {
                dataMap.put("dob",orderData.get(i));
                break;
            }
        }

        if(i-1 > -1)
        {
            dataMap.put("fatherName",orderData.get(i-1));
        }

        if(i-2 > -1)
        {
            dataMap.put("name",orderData.get(i-2));
        }

        for(i =0; i<orderData.size(); i++)
        {
            if(orderData.get(i).matches(regex))
            {
                dataMap.put("pan",orderData.get(i));
                break;
            }
        }

        return dataMap;

    }


}
