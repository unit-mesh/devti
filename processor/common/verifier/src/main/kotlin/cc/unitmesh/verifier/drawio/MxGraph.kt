package cc.unitmesh.verifier.drawio

import kotlinx.serialization.Serializable

@Serializable
data class MxFileRoot(
    val mxfile: Mxfile,
)

@Serializable
data class Mxfile(
    val diagram: Diagram,
    val attributes: Attributes?,
)

@Serializable
data class Diagram(
    val _text: String,
    val attributes: Attributes?,
)

@Serializable
data class MxGraph(
    val mxGraphModel: MxGraphModel,
)

@Serializable
data class MxGraphModel(
    val root: RootNode,
    val attributes: Attributes?,
)

@Serializable
data class RootNode(
    val mxCell: List<MXCell>,
)

@Serializable
data class MXCell(
    val mxGeometry: MXGeometry?,
    val attributes: Attributes?,
)

@Serializable
data class MXGeometry(
    val mxPoint: List<MxPoint>?,
    val Array: MXGeometry?,
    val attributes: Attributes?,
)

@Serializable
data class MxPoint(
    val attributes: Attributes?,
)

@Serializable
data class Attributes(
    val id: String,
    val name: String?,
    val host: String?,
    val modified: String?,
    val agent: String?,
    val etag: String?,
    val version: String?,
    val type: String?,
    val label: String?,
    val style: String?,
    val source: String?,
    val target: String?,
    val edge: String?,
    val value: String?,
    val vertex: String?,
    val parent: String?,
    val children: List<MXCell>?,
    val visible: Boolean?,
    val connectable: String?,
    val x: Double?,
    val y: Double?,
    val relative: String?,
    val `as`: String?,
    val width: Double?,
    val height: Double?,
    val offset: List<MxPoint>?,
)
