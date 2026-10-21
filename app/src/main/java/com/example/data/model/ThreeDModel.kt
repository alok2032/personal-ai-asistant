package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
)

data class Edge3D(
    val startIndex: Int,
    val endIndex: Int
)

data class Face3D(
    val indices: List<Int>,
    val colorHex: Long = 0xFF00F2FE
)

@Entity(tableName = "three_d_models")
data class Saved3DModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prompt: String,
    val meshType: String, // "CUBE", "PYRAMID", "CYBER_DRONE", "QUANTUM_CORE", "SPHERE", "TORUS", "HYPERCAR"
    val vertexCount: Int,
    val polygonCount: Int,
    val description: String,
    val objCode: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RenderableMesh(
    val title: String,
    val meshType: String,
    val vertices: List<Point3D>,
    val edges: List<Edge3D>,
    val faces: List<Face3D>,
    val description: String,
    val objCode: String
)
