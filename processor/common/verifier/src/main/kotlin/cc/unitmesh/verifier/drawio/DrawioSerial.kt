package cc.unitmesh.verifier.drawio

class DrawioSerial {
    fun decode(diagrams: String): String {
        val data = diagrams
            .map { it.toInt() }
            .map { it.toChar() }
            .joinToString("")
        return data
    }
}