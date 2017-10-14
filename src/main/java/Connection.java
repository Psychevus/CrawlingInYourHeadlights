import com.jayway.restassured.path.json.JsonPath;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mzaferanloo on 5/18/16.
 */
public class Connection {


    private static Document mainHtmlDocument;
    private static java.sql.Connection conn;


    public static void main(String[] args) {

        for (int a=0;a<2521;a++) {
            convertJson(a);
        }

    }


    private static void convertJson(int page) {
        try {
            String json = Jsoup.connect("http://search.digikala.com/api/search/?pageno="+page+"&pageSize=48&sortBy=10")
                    .timeout(5 * 1000)
                    .ignoreContentType(true).execute().body();

            List<String> ids = JsonPath.from(json).get("hits.hits._id");

            System.out.println(Arrays.toString(ids.toArray())); //prints hello

            for (String id : ids) {
                parse(Integer.parseInt(id));
            }

            ///http://www.digikala.com/Product/DKP-
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void parse(int id) {
        try {

            mainHtmlDocument = Jsoup.connect("http://www.digikala.com/Product/DKP-" + id)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0")
                    .timeout(5 * 1000)
                    .get();

            Elements a3D = mainHtmlDocument.select("a[id=current-product-3d]");
            if (a3D.text().equals("3D View")) {
                insertToDB(id);
            }
            System.out.println(id + " | " + a3D);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void insertToDB(int id) {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crawlinginyourheadlights?useSSL=false&useUnicode=true&characterEncoding=UTF-8", "Rabin", "icdl.xp.19");

            String insertTableSQL = "INSERT INTO 3dmodels"
                    + "(digi_id) VALUES"
                    + "(?)";

            PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();

            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
