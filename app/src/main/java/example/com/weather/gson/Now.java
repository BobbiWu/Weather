package example.com.weather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("tmp")
    public String temperature;
    @SerializedName("fl")
    public String tigan;
    @SerializedName("hum")
    public String shidu;
    @SerializedName("pcpn")
    public String water;
    public String pres;

    @SerializedName("cond")
    public More more;

    @SerializedName("wind")
    public Feng feng;

    public class More {

        @SerializedName("txt")
        public String info;

    }

    public class Feng {
        public String dir;
        public String sc;
    }

}
