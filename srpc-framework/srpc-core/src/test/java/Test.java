import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Test {
    public static void  main(String[] args) {
        // 替换为实际的连接信息
        String url = "jdbc:clickhouse://10.28.147.124:8123/default";
        String user = "root";
        String password = "1qazXSW@3edc";

        try {
            // 加载驱动
            Class.forName("ru.yandex.clickhouse.ClickHouseDriver");
            // 创建连接
            Connection connection = DriverManager.getConnection(url, user, password);
            // 创建语句
            Statement statement = connection.createStatement();
            // 执行查询，这里以查询系统表为例
            ResultSet rs = statement.executeQuery("SELECT * FROM bd_src_slave_acct LIMIT 100");
            // 处理结果
            while (rs.next()) {
                System.out.println(rs.getString("name"));  // 输出表名
            }
            // 关闭资源
            rs.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
