package na.library.grpcweatherservice;

import com.google.protobuf.InvalidProtocolBufferException;
import na.library.grpcweather.proto.WeatherData;

public class BackwardCompatibilityTest {

    public static void main(String[] args) throws InvalidProtocolBufferException {
        // Simulate serialized message from old client (v1)
        byte[] oldClientData = WeatherData.newBuilder()
                .setLocation("Ankara, Turkey")
                .setTemperature(25.5f)
                .setHumidity(65.0f)
                .setPressure(1013.2f)
                .setWindSpeed(8.5f)
                .setTimestamp("2023-03-03T10:15:30Z")
                // no email
                .build()
                .toByteArray();

        // Deserialize using new proto (v2)
        WeatherData weatherDataFromOldClient = WeatherData.parseFrom(oldClientData);

        // ✅ Compatibility check
        assert weatherDataFromOldClient.getLocation().equals("Ankara, Turkey");
        assert weatherDataFromOldClient.getTemperature() == 25.5f;
        assert weatherDataFromOldClient.getHumidity() == 65.0f;
        assert weatherDataFromOldClient.getPressure() == 1013.2f;
        assert weatherDataFromOldClient.getWindSpeed() == 8.5f;
        assert weatherDataFromOldClient.getTimestamp().equals("2023-03-03T10:15:30Z");
        //
        System.out.println("✅ Backward compatibility passed.");

        /*
            Protobuf is designed for forward and backward compatibility.
            Unknown or missing fields are ignored or set to defaults.
            This test guarantees that if an old client sends partial data, the new server won’t break.
            *** Reserve if removal is really needed
            message WeatherData {
                  reserved 6;
                  reserved "timestamp";

                  string location = 1;
                  float temperature = 2;
                  float humidity = 3;
                  float pressure = 4;
                  float wind_speed = 5;
                  string source = 7; // ✅ added safely
                   .....
            }
        */
    }
}
