import java.io.IOException;

import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

public class Students {
    public static Configuration configuration;
    public static Connection conn;
    public static Admin admin;

    //在自定义的createTable方法中调用Admin接口的createTable方法
    public static void creatTable(TableName tableName, String[] column)
            throws IOException {
        if (admin.tableExists(tableName)) {
            System.out.println("table already exists");
        }
        else {
            TableDescriptorBuilder tableDescriptor = TableDescriptorBuilder.newBuilder(tableName);
            for (String col : column) {
                ColumnFamilyDescriptor family = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(col)).build();
                tableDescriptor.setColumnFamily(family);
            }
            admin.createTable(tableDescriptor.build());
        }
    }

    public static void appendData(TableName tableName, String rowKey, String family, String column, String value) 
            throws IOException {
        Table table = conn.getTable(tableName);
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
        table.put(put);
        table.close();
    }

    public static void scan(TableName tableName) 
            throws IOException {
        Table table = conn.getTable(tableName);
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        while (scanner.iterator().hasNext()){
            Result r = scanner.next();
            for (Cell cell : r.listCells()) {
                String column = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                System.out.println(column + "：" + value);
            }
        }
        scanner.close();
    }

    public static void getProvince(TableName tableName)
            throws IOException{
        Table table = conn.getTable(tableName);
        Scan scan = new Scan();
        ResultScanner scanner = table.getScanner(scan);
        scan.addColumn(Bytes.toBytes("Home"),Bytes.toBytes("Province"));
        while (scanner.iterator().hasNext()){
            Result r = scanner.next();
            for (Cell cell : r.listCells()) {
                String column = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                System.out.println(column + "：" + value);
            }
        }
    }

    public static void addFamily (TableName tableName, String newFamily)
            throws IOException{
        ColumnFamilyDescriptor family=ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(newFamily)).build();
        admin.disableTable(tableName);
        admin.addColumnFamily(tableName,family);
        admin.enableTable(tableName);
    }

    public static void dropTable (TableName tableName) 
            throws IOException{
        //先disable再drop
        admin.disableTable(tableName);
        admin.deleteTable(tableName);
    }

    public static void main(String[] args) 
            throws Exception {
        configuration = HBaseConfiguration.create();
        conn = ConnectionFactory.createConnection(configuration);
        admin = conn.getAdmin();

        //创建表
        TableName tableName = TableName.valueOf("students");
        String[] column = {"ID", "Description", "Courses", "Home"};
        creatTable(tableName,column);
        appendData(tableName,"001","Description","Name","Li Lei");
        appendData(tableName,"002","Description","Name","Han Meimei");
        appendData(tableName,"003","Description","Name","Xiao Ming");
        appendData(tableName,"001","Description","Height","176");
        appendData(tableName,"002","Description","Height","183");
        appendData(tableName,"003","Description","Height","162");
        appendData(tableName,"001","Courses","Chinese","80");
        appendData(tableName,"002","Courses","Chinese","88");
        appendData(tableName,"003","Courses","Chinese","90");
        appendData(tableName,"001","Courses","Math","90");
        appendData(tableName,"002","Courses","Math","77");
        appendData(tableName,"003","Courses","Math","90");
        appendData(tableName,"001","Courses","Physics","95");
        appendData(tableName,"002","Courses","Physics","66");
        appendData(tableName,"003","Courses","Physics","90");
        appendData(tableName,"001","Home","Province","Zhejiang");
        appendData(tableName,"002","Home","Province","Beijing");
        appendData(tableName,"003","Home","Province","Shanghai");
        //扫描表
        scan(tableName);
        //查询来源省
        getProvince(tableName);
        //添加列并插入数据
        appendData(tableName,"001","Courses","English","99");
        appendData(tableName,"002","Courses","English","86");
        appendData(tableName,"003","Courses","English","84");
        //添加列族并添加列并插入数据
        addFamily(tableName,"Contact");
        appendData(tableName,"001","Contact","Email","lilei@163.com");
        appendData(tableName,"002","Contact","Email","hanmeimei@163.com");
        appendData(tableName,"003","Contact","Email","xiaoming@163.com");
        //删除表
        dropTable(tableName);

        admin.close();
        conn.close();
    }
}
