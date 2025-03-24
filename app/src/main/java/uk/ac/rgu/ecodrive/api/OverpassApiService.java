package uk.ac.rgu.ecodrive.api;

import uk.ac.rgu.ecodrive.models.OverpassResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OverpassApiService {
    @GET("api/interpreter")
    Call<OverpassResponse> getSpeedLimit(@Query("data") String data);
}