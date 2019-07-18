package com.ar.tsne.ardemo

import com.google.ar.sceneform.math.Vector3
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

class TSNE {

    companion object {
        val TAG = "TSNE"

        var POINTS: MutableMap<Int, Vector3> = ConcurrentHashMap()
        var WORDS: MutableMap<Int, String> = ConcurrentHashMap()
        var ITERATION = 0
    }

}