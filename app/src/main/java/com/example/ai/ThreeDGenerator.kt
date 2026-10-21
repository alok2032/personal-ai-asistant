package com.example.ai

import com.example.data.model.Edge3D
import com.example.data.model.Face3D
import com.example.data.model.Point3D
import com.example.data.model.RenderableMesh
import kotlin.math.cos
import kotlin.math.sin

object ThreeDGenerator {

    fun generateMeshForPrompt(prompt: String): RenderableMesh {
        val lower = prompt.lowercase()
        return when {
            lower.contains("drone") || lower.contains("quad") || lower.contains("rotor") -> getCyberDroneMesh()
            lower.contains("core") || lower.contains("quantum") || lower.contains("orb") || lower.contains("atom") -> getQuantumCoreMesh()
            lower.contains("car") || lower.contains("hypercar") || lower.contains("vehicle") || lower.contains("cyberpunk car") -> getHypercarMesh()
            lower.contains("ship") || lower.contains("fighter") || lower.contains("jet") || lower.contains("rocket") || lower.contains("satellite") -> getSpaceFighterMesh()
            lower.contains("diamond") || lower.contains("gem") || lower.contains("crystal") || lower.contains("poly") -> getDiamondMesh()
            lower.contains("robot") || lower.contains("arm") || lower.contains("mech") -> getRoboticsArmMesh()
            lower.contains("torus") || lower.contains("portal") || lower.contains("ring") -> getTorusPortalMesh()
            else -> getCustomProceduralMesh(prompt)
        }
    }

    fun getQuantumCoreMesh(): RenderableMesh {
        val vertices = mutableListOf<Point3D>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        // 12-vertex icosahedron approximation for Quantum Core
        val phi = 1.618f
        val scale = 80f
        val baseCoords = listOf(
            Point3D(-1f, phi, 0f), Point3D(1f, phi, 0f), Point3D(-1f, -phi, 0f), Point3D(1f, -phi, 0f),
            Point3D(0f, -1f, phi), Point3D(0f, 1f, phi), Point3D(0f, -1f, -phi), Point3D(0f, 1f, -phi),
            Point3D(phi, 0f, -1f), Point3D(phi, 0f, 1f), Point3D(-phi, 0f, -1f), Point3D(-phi, 0f, 1f)
        )
        baseCoords.forEach { p ->
            vertices.add(Point3D(p.x * scale, p.y * scale, p.z * scale))
        }

        // Add orbital ring vertices
        val ringCount = 16
        val ringRadius = 130f
        val ringStartIdx = vertices.size
        for (i in 0 until ringCount) {
            val angle = (i * 2.0 * Math.PI / ringCount).toFloat()
            vertices.add(Point3D(cos(angle) * ringRadius, 0f, sin(angle) * ringRadius))
        }
        for (i in 0 until ringCount) {
            edges.add(Edge3D(ringStartIdx + i, ringStartIdx + ((i + 1) % ringCount)))
        }

        // Icosahedron faces
        val faceIndices = listOf(
            listOf(0, 11, 5), listOf(0, 5, 1), listOf(0, 1, 7), listOf(0, 7, 10), listOf(0, 10, 11),
            listOf(1, 5, 9), listOf(5, 11, 4), listOf(11, 10, 2), listOf(10, 7, 6), listOf(7, 1, 8),
            listOf(3, 9, 4), listOf(3, 4, 2), listOf(3, 2, 6), listOf(3, 6, 8), listOf(3, 8, 9),
            listOf(4, 9, 5), listOf(2, 4, 11), listOf(6, 2, 10), listOf(8, 6, 7), listOf(9, 8, 1)
        )
        faceIndices.forEach { f ->
            faces.add(Face3D(f, 0xFF00F2FE))
            edges.add(Edge3D(f[0], f[1]))
            edges.add(Edge3D(f[1], f[2]))
            edges.add(Edge3D(f[2], f[0]))
        }

        val obj = buildObjCode("Quantum_Core", vertices, faces)
        return RenderableMesh(
            title = "Quantum Singularity Core",
            meshType = "QUANTUM_CORE",
            vertices = vertices,
            edges = edges.distinctBy { setOf(it.startIndex, it.endIndex) },
            faces = faces,
            description = "High-energy plasma containment sphere with zero-point magnetic rings and icosahedral harmonic resonance.",
            objCode = obj
        )
    }

    fun getCyberDroneMesh(): RenderableMesh {
        val vertices = mutableListOf<Point3D>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        // Central Pod (8 vertices of a hexagonal pod)
        vertices.add(Point3D(0f, 15f, 0f))    // 0: top apex
        vertices.add(Point3D(-30f, 0f, -30f)) // 1
        vertices.add(Point3D(30f, 0f, -30f))  // 2
        vertices.add(Point3D(40f, 0f, 0f))    // 3
        vertices.add(Point3D(30f, 0f, 30f))   // 4
        vertices.add(Point3D(-30f, 0f, 30f))  // 5
        vertices.add(Point3D(-40f, 0f, 0f))   // 6
        vertices.add(Point3D(0f, -20f, 0f))   // 7: bottom camera sensor

        for (i in 1..6) {
            val next = if (i == 6) 1 else i + 1
            edges.add(Edge3D(0, i))
            edges.add(Edge3D(i, next))
            edges.add(Edge3D(7, i))
            faces.add(Face3D(listOf(0, i, next), 0xFFA855F7))
        }

        // 4 Rotor Booms and Rotors
        val boomOffsets = listOf(
            Pair(-100f, -90f),
            Pair(100f, -90f),
            Pair(100f, 90f),
            Pair(-100f, 90f)
        )

        boomOffsets.forEach { (bx, bz) ->
            val boomTipIdx = vertices.size
            vertices.add(Point3D(bx, 5f, bz))
            edges.add(Edge3D(0, boomTipIdx))

            // Rotor circle
            val rotorRadius = 35f
            val rotorStart = vertices.size
            val segments = 8
            for (s in 0 until segments) {
                val a = (s * 2.0 * Math.PI / segments).toFloat()
                vertices.add(Point3D(bx + cos(a) * rotorRadius, 10f, bz + sin(a) * rotorRadius))
            }
            for (s in 0 until segments) {
                edges.add(Edge3D(rotorStart + s, rotorStart + ((s + 1) % segments)))
                edges.add(Edge3D(boomTipIdx, rotorStart + s))
            }
        }

        val obj = buildObjCode("Cyber_Tactical_Drone", vertices, faces)
        return RenderableMesh(
            title = "Autonomous Cyber Drone X-9",
            meshType = "CYBER_DRONE",
            vertices = vertices,
            edges = edges,
            faces = faces,
            description = "High-speed surveillance drone featuring quad-rotor carbon chassis, LiDAR sensor array, and stealth wing geometry.",
            objCode = obj
        )
    }

    fun getSpaceFighterMesh(): RenderableMesh {
        val vertices = mutableListOf<Point3D>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        // Cockpit nose and fuselage
        vertices.add(Point3D(0f, 0f, 130f))    // 0: Nose tip
        vertices.add(Point3D(0f, 25f, 30f))    // 1: Cockpit canopy top
        vertices.add(Point3D(-25f, -10f, -40f))// 2: Left fuselage
        vertices.add(Point3D(25f, -10f, -40f)) // 3: Right fuselage
        vertices.add(Point3D(0f, -15f, -50f))  // 4: Bottom belly
        vertices.add(Point3D(0f, 35f, -60f))   // 5: Tail fin tip
        vertices.add(Point3D(0f, 0f, -80f))    // 6: Engine exhaust center

        // Wings
        vertices.add(Point3D(-130f, -5f, -40f)) // 7: Left wing tip
        vertices.add(Point3D(130f, -5f, -40f))  // 8: Right wing tip
        vertices.add(Point3D(-60f, -5f, 20f))   // 9: Left wing canard
        vertices.add(Point3D(60f, -5f, 20f))    // 10: Right wing canard

        val wingEdges = listOf(
            Edge3D(0, 1), Edge3D(1, 6), Edge3D(0, 4), Edge3D(4, 6),
            Edge3D(1, 5), Edge3D(5, 6),
            Edge3D(0, 9), Edge3D(9, 7), Edge3D(7, 2), Edge3D(2, 6),
            Edge3D(0, 10), Edge3D(10, 8), Edge3D(8, 3), Edge3D(3, 6),
            Edge3D(1, 2), Edge3D(1, 3), Edge3D(2, 4), Edge3D(3, 4)
        )
        edges.addAll(wingEdges)

        faces.add(Face3D(listOf(0, 1, 3), 0xFF00F2FE))
        faces.add(Face3D(listOf(0, 1, 2), 0xFF00F2FE))
        faces.add(Face3D(listOf(0, 10, 8), 0xFFA855F7))
        faces.add(Face3D(listOf(0, 9, 7), 0xFFA855F7))

        val obj = buildObjCode("Space_Interceptor", vertices, faces)
        return RenderableMesh(
            title = "Astra Hypersonic Interceptor",
            meshType = "SPACE_FIGHTER",
            vertices = vertices,
            edges = edges,
            faces = faces,
            description = "Sub-orbital interceptor with swept-forward hyper-wings, dual ion exhaust ports, and titanium hull plates.",
            objCode = obj
        )
    }

    fun getHypercarMesh(): RenderableMesh {
        val vertices = mutableListOf<Point3D>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        // Low poly aerodynamic hypercar
        vertices.add(Point3D(-35f, -15f, 90f))  // 0: Front bumper left
        vertices.add(Point3D(35f, -15f, 90f))   // 1: Front bumper right
        vertices.add(Point3D(-25f, 0f, 60f))    // 2: Hood left
        vertices.add(Point3D(25f, 0f, 60f))     // 3: Hood right
        vertices.add(Point3D(-20f, 25f, 10f))   // 4: Windshield top left
        vertices.add(Point3D(20f, 25f, 10f))    // 5: Windshield top right
        vertices.add(Point3D(-22f, 22f, -40f))  // 6: Roof back left
        vertices.add(Point3D(22f, 22f, -40f))   // 7: Roof back right
        vertices.add(Point3D(-38f, -10f, -90f)) // 8: Rear diffuser left
        vertices.add(Point3D(38f, -10f, -90f))  // 9: Rear diffuser right
        vertices.add(Point3D(-42f, 10f, -95f))  // 10: Rear wing left
        vertices.add(Point3D(42f, 10f, -95f))   // 11: Rear wing right

        val carEdges = listOf(
            Edge3D(0, 1), Edge3D(0, 2), Edge3D(1, 3), Edge3D(2, 3),
            Edge3D(2, 4), Edge3D(3, 5), Edge3D(4, 5),
            Edge3D(4, 6), Edge3D(5, 7), Edge3D(6, 7),
            Edge3D(6, 10), Edge3D(7, 11), Edge3D(10, 11),
            Edge3D(10, 8), Edge3D(11, 9), Edge3D(8, 9),
            Edge3D(0, 8), Edge3D(1, 9)
        )
        edges.addAll(carEdges)
        faces.add(Face3D(listOf(2, 3, 5, 4), 0xFF10B981))
        faces.add(Face3D(listOf(4, 5, 7, 6), 0xFF00F2FE))
        faces.add(Face3D(listOf(6, 7, 11, 10), 0xFFA855F7))

        val obj = buildObjCode("Cyber_Hypercar", vertices, faces)
        return RenderableMesh(
            title = "Apex Cyber-GT Hypercar",
            meshType = "HYPERCAR",
            vertices = vertices,
            edges = edges,
            faces = faces,
            description = "Electrified futuristic hypercar concept featuring active aero rear wing, ground-effect tunnels, and laser headlamps.",
            objCode = obj
        )
    }

    fun getDiamondMesh(): RenderableMesh {
        val vertices = mutableListOf<Point3D>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        val tableRadius = 45f
        val girdleRadius = 85f
        val tableY = 40f
        val girdleY = 10f
        val culetY = -70f

        val segments = 8
        // Table vertices
        for (i in 0 until segments) {
            val a = (i * 2.0 * Math.PI / segments).toFloat()
            vertices.add(Point3D(cos(a) * tableRadius, tableY, sin(a) * tableRadius))
        }
        // Girdle vertices
        for (i in 0 until segments) {
            val a = (i * 2.0 * Math.PI / segments + (Math.PI / segments)).toFloat()
            vertices.add(Point3D(cos(a) * girdleRadius, girdleY, sin(a) * girdleRadius))
        }
        // Culet bottom tip
        val culetIdx = vertices.size
        vertices.add(Point3D(0f, culetY, 0f))

        for (i in 0 until segments) {
            val next = (i + 1) % segments
            val gIdx = segments + i
            val gNext = segments + next

            edges.add(Edge3D(i, next))
            edges.add(Edge3D(gIdx, gNext))
            edges.add(Edge3D(i, gIdx))
            edges.add(Edge3D(next, gIdx))
            edges.add(Edge3D(gIdx, culetIdx))

            faces.add(Face3D(listOf(i, next, gIdx), 0xFF00F2FE))
            faces.add(Face3D(listOf(gIdx, gNext, culetIdx), 0xFFA855F7))
        }

        val obj = buildObjCode("Brilliant_Diamond", vertices, faces)
        return RenderableMesh(
            title = "Synthetic Quantum Diamond",
            meshType = "DIAMOND",
            vertices = vertices,
            edges = edges,
            faces = faces,
            description = "Brilliant cut octagonal synthetic diamond crystal with precision refractive index and facet symmetry.",
            objCode = obj
        )
    }

    fun getRoboticsArmMesh(): RenderableMesh {
        val vertices = mutableListOf<Point3D>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        // Base pedestal
        vertices.add(Point3D(-40f, -70f, -40f)) // 0
        vertices.add(Point3D(40f, -70f, -40f))  // 1
        vertices.add(Point3D(40f, -70f, 40f))   // 2
        vertices.add(Point3D(-40f, -70f, 40f))  // 3
        // Shoulder joint
        vertices.add(Point3D(0f, -30f, 0f))     // 4
        // Elbow joint
        vertices.add(Point3D(-35f, 20f, 20f))   // 5
        // Wrist joint
        vertices.add(Point3D(20f, 70f, 10f))    // 6
        // End effector claws
        vertices.add(Point3D(10f, 95f, 25f))    // 7
        vertices.add(Point3D(30f, 95f, -5f))    // 8

        edges.addAll(listOf(
            Edge3D(0, 1), Edge3D(1, 2), Edge3D(2, 3), Edge3D(3, 0),
            Edge3D(0, 4), Edge3D(1, 4), Edge3D(2, 4), Edge3D(3, 4),
            Edge3D(4, 5), Edge3D(5, 6),
            Edge3D(6, 7), Edge3D(6, 8)
        ))

        faces.add(Face3D(listOf(0, 1, 4), 0xFFF59E0B))
        faces.add(Face3D(listOf(1, 2, 4), 0xFF10B981))

        val obj = buildObjCode("Robotics_Arm", vertices, faces)
        return RenderableMesh(
            title = "Articulated Industrial Bionic Arm",
            meshType = "ROBOTIC_ARM",
            vertices = vertices,
            edges = edges,
            faces = faces,
            description = "6-Axis kinematic robotic manipulator with dual harmonic drives, carbon fiber links, and precision gripper.",
            objCode = obj
        )
    }

    fun getTorusPortalMesh(): RenderableMesh {
        val vertices = mutableListOf<Point3D>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        val majorRadius = 90f
        val minorRadius = 30f
        val uSegments = 12
        val vSegments = 8

        for (u in 0 until uSegments) {
            val uAngle = (u * 2.0 * Math.PI / uSegments).toFloat()
            for (v in 0 until vSegments) {
                val vAngle = (v * 2.0 * Math.PI / vSegments).toFloat()
                val x = (majorRadius + minorRadius * cos(vAngle)) * cos(uAngle)
                val y = minorRadius * sin(vAngle)
                val z = (majorRadius + minorRadius * cos(vAngle)) * sin(uAngle)
                vertices.add(Point3D(x, y, z))
            }
        }

        for (u in 0 until uSegments) {
            val nextU = (u + 1) % uSegments
            for (v in 0 until vSegments) {
                val nextV = (v + 1) % vSegments
                val idx0 = u * vSegments + v
                val idx1 = nextU * vSegments + v
                val idx2 = nextU * vSegments + nextV
                val idx3 = u * vSegments + nextV

                edges.add(Edge3D(idx0, idx1))
                edges.add(Edge3D(idx0, idx3))
                faces.add(Face3D(listOf(idx0, idx1, idx2), 0xFF00F2FE))
            }
        }

        val obj = buildObjCode("Quantum_Portal", vertices, faces)
        return RenderableMesh(
            title = "Einstein-Rosen Quantum Portal Ring",
            meshType = "TORUS_PORTAL",
            vertices = vertices,
            edges = edges,
            faces = faces,
            description = "Toroidal spacetime gateway mesh with warp field coils and quantum curvature boundary.",
            objCode = obj
        )
    }

    private fun getCustomProceduralMesh(prompt: String): RenderableMesh {
        val vertices = mutableListOf<Point3D>()
        val edges = mutableListOf<Edge3D>()
        val faces = mutableListOf<Face3D>()

        // Generate dynamic futuristic polyhedron based on prompt hash
        val hash = prompt.hashCode()
        val tiers = 4
        val pointsPerTier = 6
        val heightStep = 45f

        for (t in 0 until tiers) {
            val y = (t - (tiers - 1) / 2f) * heightStep
            val radius = 50f + 30f * sin((t + (hash % 10)) * 1.2f)
            val angleOffset = t * 0.4f
            for (p in 0 until pointsPerTier) {
                val a = (p * 2.0 * Math.PI / pointsPerTier + angleOffset).toFloat()
                val x = cos(a) * radius
                val z = sin(a) * radius
                vertices.add(Point3D(x, y, z))
            }
        }

        for (t in 0 until (tiers - 1)) {
            for (p in 0 until pointsPerTier) {
                val nextP = (p + 1) % pointsPerTier
                val i0 = t * pointsPerTier + p
                val i1 = t * pointsPerTier + nextP
                val i2 = (t + 1) * pointsPerTier + nextP
                val i3 = (t + 1) * pointsPerTier + p

                edges.add(Edge3D(i0, i1))
                edges.add(Edge3D(i0, i3))
                faces.add(Face3D(listOf(i0, i1, i2), 0xFFA855F7))
            }
        }

        val title = prompt.trim().split(" ").take(4).joinToString(" ").replaceFirstChar { it.uppercase() }
        val obj = buildObjCode("Bidiok_Generative_Mesh", vertices, faces)
        return RenderableMesh(
            title = "$title 3D Structure",
            meshType = "CUSTOM_GENERATIVE",
            vertices = vertices,
            edges = edges,
            faces = faces,
            description = "AI-generated parametric 3D structure synthesized from: '$prompt'. Clean CAD geometry with balanced topological vertices.",
            objCode = obj
        )
    }

    private fun buildObjCode(name: String, vertices: List<Point3D>, faces: List<Face3D>): String {
        val sb = StringBuilder()
        sb.append("# Bidiok AI 3D Studio Wavefront OBJ File\n")
        sb.append("# Object Name: $name\n")
        sb.append("# Vertices: ${vertices.size}, Faces: ${faces.size}\n\n")
        sb.append("o $name\n\n")

        vertices.forEach { v ->
            sb.append(String.format("v %.4f %.4f %.4f\n", v.x / 100f, v.y / 100f, v.z / 100f))
        }

        sb.append("\ns 1\n")
        faces.take(150).forEach { f ->
            val faceIndices = f.indices.map { it + 1 }.joinToString(" ")
            sb.append("f $faceIndices\n")
        }
        return sb.toString()
    }
}
