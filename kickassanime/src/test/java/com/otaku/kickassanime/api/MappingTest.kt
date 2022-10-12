import com.fetch.cloudflarebypass.CloudflareHTTPClient
import com.google.gson.GsonBuilder
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.conveter.FindJsonInTextConverterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MappingTest {

    private lateinit var kickassAnimeService: KickassAnimeService

    @Before
    fun setup() {
        kickassAnimeService = Retrofit.Builder()
            .addConverterFactory(FindJsonInTextConverterFactory.create(GsonBuilder().serializeNulls().create()))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
            .client(CloudflareHTTPClient(
                object: com.fetch.cloudflarebypass.Log {
                    override fun i(tag: String, s: String) {
                        println("$tag, $s")
                    }

                    override fun e(tag: String, s: String) {
                        println("$tag, $s")
                    }

                }
            ).okHttpClient.build())
            .baseUrl(Strings.KICKASSANIME_URL)
            .build()
            .create(KickassAnimeService::class.java)
    }

    @Test
    fun testAnimeInformationToAnimeEntity(){
        runBlocking {
//            val animeInformation =
//                kickassAnimeService.getAnimeInformation("/anime/dungeon-ni-deai-wo-motomeru-no-wa-machigatteiru-darou-ka-iv-shin-shou-meikyuu-hen-732841")
//            val asAnimeEntity = animeInformation.asAnimeEntity()
//            assertNotNull(asAnimeEntity)
        }
    }
}