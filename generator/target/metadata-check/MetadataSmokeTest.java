import java.sql.DriverManager;
import java.util.List;
import com.hr.generator.DatabaseMetadataReader;
import com.hr.generator.ColumnMeta;

public class MetadataSmokeTest {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://localhost:3306/hr_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    List<String> tables = DatabaseMetadataReader.tables(url, "root", "root");
    System.out.println("tables=" + tables.size());
    if (tables.isEmpty() || !tables.contains("hr_employee")) {
      throw new IllegalStateException("hr_employee table missing");
    }
    var t = DatabaseMetadataReader.table(url, "root", "root", "hr_employee");
    System.out.println("class=" + t.className() + " comment=" + t.comment() + " columns=" + t.columns().size());
    t.columns().forEach(c -> System.out.println(c.javaType() + " " + c.fieldName() + (c.primaryKey() ? " PK" : "") + " // " + c.comment()));
    long pk = t.columns().stream().filter(ColumnMeta::primaryKey).count();
    if (pk != 1) throw new IllegalStateException("expected exactly 1 primary key column, got " + pk);
    if (t.columns().stream().noneMatch(c -> "id".equals(c.name()))) throw new IllegalStateException("id column missing");
    if (t.comment().isBlank()) throw new IllegalStateException("table comment missing");
    if (t.columns().stream().filter(c -> !c.sensitive() && !"id".equals(c.name())).anyMatch(c -> c.comment().isBlank())) throw new IllegalStateException("column comment missing (non-sensitive, non-id)");
  }
}
