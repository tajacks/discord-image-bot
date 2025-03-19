package dev.tomjack.bots.imagebot.client

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class FireworksClient(
    private val httpClient: HttpClient,
) {
    suspend fun getImage(prompt: String): ByteArray =
        httpClient
            .post(URL) {
                contentType(ContentType.Application.Json)
                header("Accept", ContentType.Image.JPEG)
                setBody(
                    FireworksRequest(
                        prompt = prompt,
                        aspectRatio = "16:9",
                        guidanceScale = 3.5,
                        numInferenceSteps = 23,
                        seed = Random.nextLong(0, 1000000),
                    ),
                )
            }.readRawBytes()

    companion object {
        private const val URL =
            "https://api.fireworks.ai/inference/v1/workflows/accounts/fireworks/models/flux-1-dev-fp8/text_to_image"
    }
}

@Serializable
data class FireworksRequest(
    val prompt: String,
    @SerialName("aspect_ratio")
    val aspectRatio: String,
    @SerialName("guidance_scale")
    val guidanceScale: Double,
    @SerialName("num_inference_steps")
    val numInferenceSteps: Int,
    val seed: Long,
)
